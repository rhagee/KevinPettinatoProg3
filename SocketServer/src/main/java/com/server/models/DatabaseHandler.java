package com.server.models;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public enum DatabaseHandler {
    INSTANCE;

    //region Constants
    private static final Logger LOGGER = Logger.getLogger("DatabaseHandler");

    private static final String secret = "PROG3EXAM";
    private static final String issuer = "MailServer";

    public static final String dbName = "MailServer_Database";
    private static final String USERS_FILE_NAME = "users.json";
    private static final String MAILBOX_FILE_NAME = "mailbox.json";
    private static final String SENT_CHUNKS_DIR_NAME = "sent_chunks";
    private static final String RECEIVED_CHUNKS_DIR_NAME = "received_chunks";
    //endregion

    //region Statics
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).build();

    public static File CACHED_DIR;
    private static File CACHED_USER_FILE;
    //endregion

    //region Properties
    private final Accounts accounts = new Accounts();
    private boolean isInitialized = false;
    //endregion

    //region DirUtils
    private static File USER_FILE() {
        if (CACHED_USER_FILE != null) {
            return CACHED_USER_FILE;
        }

        CACHED_USER_FILE = new File(DEFAULT_DIR(), USERS_FILE_NAME);

        return CACHED_USER_FILE;
    }

    public static File DEFAULT_DIR() {
        if (CACHED_DIR != null) {
            return CACHED_DIR;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String path;

        if (os.contains("win")) {
            LOGGER.info("OS : Windows");
            path = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            LOGGER.info("OS : iOS");
            path = System.getProperty("user.home") + "/Library/Application Support";
        } else if (os.contains("linux")) {
            LOGGER.info("OS : Linux");
            path = System.getProperty("user.home") + "./local/share";
        } else {
            System.err.println("OS not supported!");
            return null;
        }

        File dir = new File(path, dbName);
        if (!dir.exists()) {
            LOGGER.info("Creating missing data path...");
            if (!dir.mkdirs()) {
                LOGGER.severe("Failed to create missing data path!");
                System.err.println("Can't create directory at path: " + path + ".\nPlease run the process as Administrator and Write access to that path.");
                return null;
            }
        }
        CACHED_DIR = dir;
        return dir;
    }
    //endregion

    //region Flow
    public synchronized void Initialize() {
        LOGGER.info("Initializing DatabaseHandler...");
        if (isInitialized) {
            return;
        }

        if (!LoadAccounts()) {
            return;
        }

        LOGGER.info("DatabaseHandler Initialized successfully!");
        isInitialized = true;
    }
    //endregion

    //region Query

    public synchronized QueryResult<String> authUser(String mail) {
        QueryResult<String> result = new QueryResult<>();

        if (accounts.authMail(mail)) {
            String token = GenerateToken(mail);
            result.SetSuccessPayload(token);
        } else {
            result.Error("Mail non presente nel sistema!");
        }

        return result;
    }

    public synchronized QueryResult<List<Mail>> getMailPage() {

        if (!isInitialized) {
            Initialize();
        }

        return null;
    }

    public synchronized QueryResult<?> sendMail(SmallMail mail) {

        QueryResult<Mail> result = new QueryResult<>();

        if (!isInitialized) {
            Initialize();
        }

        //Should be checked client-side but just safe check here
        if (mail.getReceiverList() == null) {
            LOGGER.info("Impossibile inviare email di " + mail.getSender() + ". Nessun destinatario nella mail.");
            result.Error("Email ricevuta senza destinatari. Si prega di scrivere almeno 1 destinatario.");
            return result;
        }

        //Check for existing receivers
        List<String> unknownMails = new ArrayList<>();
        for (String receiver : mail.getReceiverList()) {
            if (!accounts.mailExists(receiver)) {
                unknownMails.add(receiver);
            }
        }

        //If there are non existing receivers
        if (!unknownMails.isEmpty()) {
            LOGGER.info("Impossibile inviare email di " + mail.getSender() + ". Non trovati : " + unknownMails.size() + "/" + mail.getReceiverList().size());
            result.Error("Mail non riconosciute : " + String.join(",", unknownMails));
            return result;
        }

        //If can't add to sent
        if (!addSent(mail.getSender(), mail)) {
            LOGGER.info("Impossibile inviare la mail di " + mail.getSender() + ". Errore nella fase di aggiunta invio.");
            result.Error("Impossibile inviare la mail.");
            return result;
        }

        //Try to send to everyone
        List<String> notReceivedList = new ArrayList<>();
        for (String receiver : mail.getReceiverList()) {
            if (!addReceived(receiver, mail)) {
                notReceivedList.add(receiver);
            }
        }

        if (!notReceivedList.isEmpty()) {
            LOGGER.warning("Errore imprevisto, impossibile inviare ad alcuni destinatari.");
            result.Success("Errore imprevisto, impossibile inviare ai destinatari " + String.join(",", notReceivedList));
        }

        return result;
    }
    //endregion

    //region QueryUtils
    private synchronized List<Mail> getMailsRange(List<ChunkRange> ranges, String dirPath) {
        List<Mail> mails = new ArrayList<>();

        for (ChunkRange range : ranges) {
            int from = range.getStart();
            int to = range.getEnd();
            UUID chunkID = range.getId();
            MailBoxChunk chunk = GetMailboxChunk(chunkID, dirPath);
            if (chunk != null) {
                mails.addAll(chunk.getMailFromTo(from, to));
            }
        }

        return mails;
    }
    //endregion

    //region AddMail
    private synchronized boolean addSent(String mail, SmallMail smallMail) {
        return addMail(mail, smallMail, getSentChunksPath(mail));
    }

    private synchronized boolean addReceived(String mail, SmallMail smallMail) {
        return addMail(mail, smallMail, getReceivedChunksPath(mail));
    }

    private synchronized boolean addMail(String mail, SmallMail smallMail, String dir) {
        if (!accounts.mailExists(mail)) {
            return false;
        }

        Mail newMail = new Mail(smallMail);
        MailBox mailBox = getMailBox(mail);
        if (mailBox == null) {
            return false;
        }

        UUID chunkID;
        if (mail.equals(smallMail.getSender())) {
            chunkID = mailBox.addSent();
        } else {
            chunkID = mailBox.addReceived();
        }

        newMail.setChunkID(chunkID);
        MailBoxChunk chunk = GetMailboxChunk(chunkID, dir);

        if (chunk == null) {
            chunk = CreateMailboxChunk(chunkID, mail, dir);
        }

        if (chunk == null) {
            return false;
        }

        chunk.AddMail(newMail);
        return SaveMailBox(mailBox) && SaveMailboxChunk(chunk, dir);
    }
    //endregion

    //region Accounts
    public synchronized boolean createAccount(String mail) {
        LOGGER.info("Creating new account : " + mail);
        if (!isInitialized) {
            Initialize();
        }

        try {
            boolean result = accounts.addMail(mail);
            if (result) {
                MAPPER.writeValue(USER_FILE(), accounts.getMails());
            }

            MailBox mailBox = CreateMailBox(mail);
            result &= mailBox != null;

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private synchronized boolean LoadAccounts() {
        try {
            LOGGER.info("Loading accounts...");
            File userFile = USER_FILE();
            if (!userFile.exists()) {
                MAPPER.writeValue(userFile, new ArrayList<>());
            }

            accounts.setMails(MAPPER.readValue(userFile, List.class));
            LOGGER.info("Accounts loaded successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized String GenerateToken(String email) throws JWTVerificationException {
        return JWT.create().withIssuer(issuer).withPayload(email).sign(Algorithm.HMAC256(secret));
    }

    public synchronized String DecodeToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = jwtVerifier.verify(token);
        return jwt.getPayload();
    }

    public void addAccountsListener(ChangeListener<List<String>> changeListener) {
        accounts.addListener(changeListener);
    }

    public void removeAccountsListener(ChangeListener<List<String>> changeListener) {
        accounts.removeListener(changeListener);
    }

    public List<String> getAccountsList() {
        return accounts.getMails();
    }
    //endregion

    //region PathUtils

    private String getMailboxDir(String mail) {
        return DEFAULT_DIR() + "/" + mail;
    }

    private String getMailboxPath(String mail) {
        return getMailboxDir(mail) + "/" + MAILBOX_FILE_NAME;
    }

    private String getReceivedChunksPath(String mail) {
        return getMailboxDir(mail) + "/" + RECEIVED_CHUNKS_DIR_NAME;
    }

    private String getSentChunksPath(String mail) {
        return getMailboxDir(mail) + "/" + SENT_CHUNKS_DIR_NAME;
    }
    //endregion

    //region MailBox
    private synchronized MailBox getMailBox(String mail) {
        File mailBox = new File(getMailboxPath(mail));
        try {
            if (!mailBox.exists()) {
                return CreateMailBox(mail);
            }

            return MAPPER.readValue(mailBox, MailBox.class);
        } catch (IOException e) {
            LOGGER.severe("Can't read Mailbox file at " + mailBox.getAbsolutePath());
            return null;
        }
    }

    private synchronized MailBox CreateMailBox(String mail) {
        try {
            MailBox mailBox = new MailBox(mail);
            File mailBoxDir = new File(DEFAULT_DIR(), mail);
            if (!mailBoxDir.exists()) {
                if (!mailBoxDir.mkdir()) {
                    LOGGER.severe("Failed to create mailbox directory: " + mailBoxDir.getAbsolutePath());
                    return null;
                }
            }

            File mailBoxFile = new File(mailBoxDir, MAILBOX_FILE_NAME);
            MAPPER.writeValue(mailBoxFile, mailBox);

            File receivedDir = new File(mailBoxDir, RECEIVED_CHUNKS_DIR_NAME);
            if (!receivedDir.mkdir()) {
                LOGGER.severe("Failed to create chunks directory: " + receivedDir.getAbsolutePath());
                return null;
            }

            File sentDir = new File(mailBoxDir, SENT_CHUNKS_DIR_NAME);
            if (!sentDir.mkdir()) {
                LOGGER.severe("Failed to create chunks directory: " + sentDir.getAbsolutePath());
                return null;
            }

            return mailBox;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized boolean SaveMailBox(MailBox mailBox) {
        try {
            String mail = mailBox.getMail();
            String mailBoxPath = getMailboxPath(mail);
            File mailBoxFile = new File(mailBoxPath);
            if (!mailBoxFile.exists()) {
                MailBox temp = CreateMailBox(mail);
                if (temp == null) {
                    return false;
                }
            }

            MAPPER.writeValue(mailBoxFile, mailBox);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Can't Save MailBox for mail :" + mailBox.getMail());
            e.printStackTrace();
            return false;
        }
    }

    //endregion

    //region MailBoxChunk
    private synchronized MailBoxChunk CreateMailboxChunk(UUID id, String mail, String path) {
        try {
            File chunkFile = new File(path, id + ".json");

            //IF already exists just return the existing one
            if (chunkFile.exists()) {
                return MAPPER.readValue(chunkFile, MailBoxChunk.class);
            }

            MailBoxChunk chunk = new MailBoxChunk(id, mail);
            MAPPER.writeValue(chunkFile, chunk);
            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized MailBoxChunk GetMailboxChunk(UUID id, String dirPath) {
        try {
            if (id == null) {
                return null;
            }

            File chunkFile = new File(dirPath, id + ".json");
            if (!chunkFile.exists()) {
                return null;
            }

            return MAPPER.readValue(chunkFile, MailBoxChunk.class);
        } catch (IOException e) {
            LOGGER.severe("Can't read Chunk file. Potential different version, clean the database to continue.");
            e.printStackTrace();
            return null;
        }
    }

    private synchronized boolean SaveMailboxChunk(MailBoxChunk chunk, String dirPath) {
        try {
            MAPPER.writeValue(new File(dirPath, chunk.getChunkID() + ".json"), chunk);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Can't save chunk");
            e.printStackTrace();
            return false;
        }
    }
    //endregion

}
