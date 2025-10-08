package com.server.models;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.Accounts;
import communication.Mail;
import communication.MailBoxChunk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public enum DatabaseHandler {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger("DatabaseHandler");
    private static final String secret = "PROG3EXAM";
    private static final String issuer = "MailServer";
    public static final String dbName = "MailServer_Database";
    private static final String USERS_FILE_NAME = "users.json";

    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).build();

    public static File CACHED_DIR;
    private static File CACHED_USER_FILE;

    private Accounts accounts;
    private boolean isInitialized = false;

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

    public synchronized boolean CreateAccount(String mail) {

        LOGGER.info("Creating new account : " + mail);
        if (!isInitialized) {
            Initialize();
        }

        return true;
    }

    public synchronized boolean DeleteAccount(String mail) {
        LOGGER.info("Deleting existing account : " + mail);
        if (!isInitialized) {
            Initialize();
        }

        return true;
    }

    public synchronized List<Mail> GetMailPage() {

        if (!isInitialized) {
            Initialize();
        }

        return null;
    }

    public synchronized void SendMail() {
        if (!isInitialized) {
            Initialize();
        }

    }

    public synchronized List<Mail> PollMail() {
        if (!isInitialized) {
            Initialize();
        }

        return null;
    }

    private synchronized List<Mail> readEmails(List<MailBoxChunk> chunks) {
        List<Mail> mails = new ArrayList<Mail>();

        //READ FROM FILE CHUNKS

        return mails;
    }

    public synchronized String checkUser(String mail) {
        if (accounts == null) {
            return null;
        }

        if (accounts.authMail(mail)) {
            return GenerateToken(mail);
        } else {
            return null;
        }
    }

    public synchronized String GenerateToken(String email) throws JWTVerificationException {
        return JWT.create().withIssuer(issuer).withPayload(email).sign(Algorithm.HMAC256(secret));
    }

    public synchronized String DecodeToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = jwtVerifier.verify(token);
        return jwt.getPayload();
    }

    private synchronized boolean LoadAccounts() {
        try {
            LOGGER.info("Loading accounts...");
            File userFile = USER_FILE();
            if (!userFile.exists()) {
                Accounts accounts = new Accounts();
                MAPPER.writeValue(userFile, accounts);
            }

            accounts = MAPPER.readValue(userFile, Accounts.class);
            LOGGER.info("Accounts loaded successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
