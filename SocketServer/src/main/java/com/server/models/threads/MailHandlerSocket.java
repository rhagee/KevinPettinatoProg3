package com.server.models.threads;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.server.models.DatabaseHandler;
import communication.*;
import utils.RequestCodes;
import utils.ResponseCodes;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailHandlerSocket extends Thread {

    private static final Logger LOGGER = Logger.getLogger("SocketWorker");

    private UUID id;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    private String mail;
    private Object requestObj;


    public MailHandlerSocket(UUID id, Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.id = id;
        this.client = client;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        LOGGER.info("Listening Requests from client: " + client.getRemoteSocketAddress());
        while (!Thread.currentThread().isInterrupted() && TryReadObject()) {

            //Exception
            if (requestObj == null) {
                continue;
            }

            //Wrong Type Check + Assign request with Cast
            if (!(requestObj instanceof Request<?> request)) {
                LOGGER.log(Level.WARNING, "Can't parse requestedObject", requestObj);
                continue;
            }

            HandleRequest(request);
        }
    }

    private void HandleRequest(Request<?> request) {
        RequestCodes code = request.getCode();
        switch (code) {
            case RequestCodes.AUTH:
                OnAuth(request);
                break;
            case RequestCodes.MAILBOX:
                OnMailbox(request);
                break;
            case RequestCodes.RECEIVE:
                OnReceive(request);
                break;
            case RequestCodes.SEND:
                OnSend(request);
                break;
            case RequestCodes.DELETE:
                OnDelete(request);
                break;
            case RequestCodes.READ:
                OnReadUnread(request, true);
                break;
            case RequestCodes.UNREAD:
                OnReadUnread(request, false);
                break;
            default:
                OnNotFound(request);
                break;
        }
    }

    private void OnNotFound(Request<?> request) {
        LOGGER.log(Level.WARNING, "Can't handle request with code", request.getCode().toString());
        SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.NOTFOUND));
    }

    private void OnAuth(Request<?> request) {
        if (!TypeCheck(request, String.class)) {
            return;
        }

        String mail = (String) request.getPayload();
        QueryResult<String> result = DatabaseHandler.INSTANCE.authUser(mail);
        if (result.isError()) {
            LOGGER.info("Email : " + mail + " is not a valid account");
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED, result.getMessage()));
            return;
        }

        String token = result.getPayload();
        this.mail = mail;
        SocketHandler.registerMailHandler(mail, this);

        LOGGER.info("Login with email : " + mail + " successful! Token generated and sent to client.");
        SendObject(new Response<>(request.getRequestID(), token, ResponseCodes.OK));
    }

    private void OnReceive(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }

        if (!TypeCheck(request, MailPageRequest.class)) {
            return;
        }


        MailPageRequest pageRequest = (MailPageRequest) request.getPayload();

        String receivedSentString = pageRequest.isFromReceived() ? "Received" : "Sent";
        LOGGER.info(this.mail + " - Requested " + receivedSentString + " page with start: " + pageRequest.getStart() + " and quantity: " + pageRequest.getQuantity());
        QueryResult<List<Mail>> mailListResult = DatabaseHandler.INSTANCE.getMailPage(this.mail, pageRequest);
        if (mailListResult.isError()) {
            LOGGER.warning(this.mail + " - Failed requesting " + receivedSentString + " page with error: " + mailListResult.getMessage());
            SendObject(new Response<>(request.getRequestID(), null, ResponseCodes.UNHANDLED, mailListResult.getMessage()));
            return;
        }

        List<Mail> mailList = mailListResult.getPayload();

        LOGGER.info(this.mail + " - Success requesting " + receivedSentString + " page ");
        SendObject(new Response<>(request.getRequestID(), mailList, ResponseCodes.OK));
    }

    private void OnMailbox(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }

        LOGGER.info(this.mail + " - Requested his mailbox metadata");
        QueryResult<MailBoxMetadata> mailBoxResult = DatabaseHandler.INSTANCE.getMailBoxMetadata(this.mail);
        if (mailBoxResult.isError()) {
            LOGGER.warning(this.mail + " - Failed getting his mailbox metadata with error: " + mailBoxResult.getMessage());
            SendObject(new Response<>(request.getRequestID(), 0, ResponseCodes.UNHANDLED, mailBoxResult.getMessage()));
            return;
        }

        LOGGER.info(this.mail + " - Success getting his mailbox metadata");
        MailBoxMetadata mailBoxMetadata = mailBoxResult.getPayload();
        SendObject(new Response<>(request.getRequestID(), mailBoxMetadata, ResponseCodes.OK));
    }

    private void OnSend(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }

        if (!TypeCheck(request, SmallMail.class)) {
            return;
        }

        LOGGER.info(this.mail + " - Trying to send an email");
        SmallMail toSend = (SmallMail) request.getPayload();
        QueryResult<Mail> sendResult = DatabaseHandler.INSTANCE.sendMail(toSend);
        if (sendResult.isError()) {
            LOGGER.warning(this.mail + " - Failed sending an email with error: " + sendResult.getMessage());
            SendObject(new Response<>(request.getRequestID(), 0, ResponseCodes.UNHANDLED, sendResult.getMessage()));
            return;
        }

        LOGGER.info(this.mail + " - Email sent with success");
        SendObject(new Response<>(request.getRequestID(), sendResult.getPayload(), ResponseCodes.OK));
    }

    private void OnDelete(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }

        if (!TypeCheck(request, Mail.class)) {
            return;
        }

        LOGGER.info(this.mail + " - Trying to delete an email");
        Mail toDelete = (Mail) request.getPayload();
        QueryResult<?> deleteResult = DatabaseHandler.INSTANCE.deleteMail(this.mail, toDelete);
        if (deleteResult.isError()) {
            LOGGER.warning(this.mail + " - Failed deleting an email with error: " + deleteResult.getMessage());
            SendObject(new Response<>(request.getRequestID(), 0, ResponseCodes.UNHANDLED, deleteResult.getMessage()));
            return;
        }

        LOGGER.info(this.mail + " - Email deleted with success");
        SendObject(new Response<String>(request.getRequestID(), deleteResult.getMessage(), ResponseCodes.OK));
    }


    private void OnReadUnread(Request<?> request, boolean read) {
        if (!isAuthenticated(request)) {
            return;
        }

        if (!TypeCheck(request, Mail.class)) {
            return;
        }

        String readUnreadString = read ? "read" : "unread";
        LOGGER.info(this.mail + " - Trying to " + readUnreadString + " an email");
        Mail toRead = (Mail) request.getPayload();
        QueryResult<?> readUnreadResult = DatabaseHandler.INSTANCE.readUnreadMail(this.mail, toRead, read);
        if (readUnreadResult.isError()) {
            LOGGER.info(this.mail + " - Failed to " + readUnreadString + " an email with error: " + readUnreadResult.getMessage());
            SendObject(new Response<>(request.getRequestID(), 0, ResponseCodes.UNHANDLED, readUnreadResult.getMessage()));
            return;
        }

        LOGGER.info(this.mail + " - Success to " + readUnreadString + " an email");
        SendObject(new Response<String>(request.getRequestID(), readUnreadResult.getMessage(), ResponseCodes.OK));
    }

    private boolean TypeCheck(Request<?> request, Class c) {

        if (!c.isInstance(request.getPayload())) {
            LOGGER.log(Level.WARNING, "Request " + request.getRequestID() + " has invalid payload type, expected " + c.toString() + " received " + request.getPayload().getClass());
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.BADPARAM));
            return false;
        }

        return true;
    }

    private boolean isAuthenticated(Request<?> request) {
        try {
            String token = request.getToken();
            this.mail = DatabaseHandler.INSTANCE.DecodeToken(token); //Refresh Mail in case we'v lost it (re-connection)
            return true;
        } catch (JWTVerificationException exception) {
            LOGGER.log(Level.INFO, "Client " + client.getInetAddress().toString() + " not authorized to " + request.getCode().toString());
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED));
            return false;
        }
    }

    private synchronized void SendObject(Response<?> res) {
        try {
            out.writeObject(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean TryReadObject() {
        try {
            return (requestObj = in.readObject()) != null;
        } catch (IOException e) {
            LOGGER.info("Connection with " + client.getRemoteSocketAddress() + " closed.");
            SocketHandler.removeHandler(id, mail);
            return false;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Class not found", e);
            requestObj = null;
            return true;
        }
    }

    public void sendMail(Response<?> res) {
        SendObject(res);
    }

    @Override
    public void interrupt() {
        try {
            LOGGER.info("Interrupting connection with " + client.getRemoteSocketAddress());

            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException exception) {
            LOGGER.severe("Exception while interrupting thread");
        } finally {
            super.interrupt();
        }

    }

}
