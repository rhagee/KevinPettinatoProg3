package com.client.models.BackendManagement;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.EmailManagement.MailBoxManager;
import com.client.models.ProgApplication;
import communication.Mail;
import communication.Request;
import communication.Response;
import utils.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BackendEventReceiver implements Runnable {

    private ObjectInputStream in;
    private final ConcurrentHashMap<String, CompletableFuture<Response<?>>> pendingRequests = new ConcurrentHashMap<>();

    private Object raw;

    @Override
    public void run() {
        try {

            //Get the input reader
            this.in = BackendManager.INSTANCE.getIn();

            while (TryReadFromThreadBuffer()) {
                Response<?> response = (Response<?>) raw;

                //Send it to the right request callback
                if (TrySendToRequest(response)) {
                    continue;
                }

                HandleEvent(response);
            }
        } catch (IOException exception) {
            if (ProgApplication.APPLICATION_CLOSED) {
                System.out.println("EventReceiver connection closed by Closing Application clean-up");
                return;
            }

            new Thread(BackendManager.INSTANCE::ConnectionDropped).start();
        } catch (ClassNotFoundException | ClassCastException exception) {
            if (ProgApplication.APPLICATION_CLOSED) {
                return;
            }
            AlertManager.get().add("Errore non riconosciuto", "Potenziale mismatch di versione tra il client ed il server, controllare.", AlertType.ERROR);
            exception.printStackTrace();
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    public boolean TrySendToRequest(Response<?> response) {
        String id = response.getRequestID();
        if (id != null && pendingRequests.containsKey(id)) {
            CompletableFuture<Response<?>> callback = pendingRequests.remove(id);

            if (callback != null) {
                callback.complete(response);
            }

            return true;
        }

        return false;
    }

    public void AddPendingRequest(String id, CompletableFuture<Response<?>> callback) {
        if (callback == null) {
            pendingRequests.put(id, new CompletableFuture<>());
            return;
        }

        pendingRequests.put(id, callback);
    }

    private boolean TryReadFromThreadBuffer() throws IOException, ClassNotFoundException {
        return (this.raw = in.readObject()) != null;
    }

    private void HandleEvent(Response<?> response) {
        if (response.getCode() != ResponseCodes.OK && response.getCode() != ResponseCodes.UPDATE) {
            AlertManager.get().add("Errore", response.getErrorMessage(), AlertType.ERROR);
            return;
        }

        //This can be used to handle different "passive-listen" flows
        switch (response.getCode()) {
            case ResponseCodes.UPDATE:
                MailBoxManager.INSTANCE.mailReceived(response);
                break;
            default:
                //Decide if to show a warning in Client UI or just discard
                break;

        }
    }

    //Throw exception on all pending requests
    public void failAllPending(Throwable ex) {
        pendingRequests.forEach((id, callback) -> callback.completeExceptionally(ex));
        pendingRequests.clear();
    }

    public Object getRaw() {
        return this.raw;
    }

}
