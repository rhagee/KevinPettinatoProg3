package com.server.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.FileSystemException;

public enum DatabaseHandler {
    INSTANCE;

    public static final String appName = "MailServer";
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static File CACHED_DIR;
    private boolean isInitialized = false;
    private static File INTERNAL_CACHED_USER_DIR = new File(DEFAULT_DIR(), "users.json");

    public static File DEFAULT_DIR() {
        if (CACHED_DIR != null) {
            return CACHED_DIR;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String path;

        if (os.contains("win")) {
            path = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            path = System.getProperty("user.home") + "/Library/Application Support";
        } else if (os.contains("linux")) {
            path = System.getProperty("user.home") + "./local/share";
        } else {
            System.err.println("OS not supported!");
            return null;
        }

        File dir = new File(path, appName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Can't create directory at path: " + path + ".\nPlease run the process as Administrator and Write access to that path.");
                return null;
            }
        }
        CACHED_DIR = dir;
        return dir;
    }

    public synchronized void Initialize() {
        isInitialized = true;
    }

    public synchronized void Insert() {

        if (!isInitialized) {
            Initialize();
        }

    }

    public synchronized void Update() {
        if (!isInitialized) {
            Initialize();
        }

    }

    public synchronized Object Select() {

        if (!isInitialized) {
            Initialize();
        }

        return null;
    }

    public synchronized boolean CheckUser(String mail) {
        return true;
    }

    public synchronized void Delete() {
        if (!isInitialized) {
            Initialize();
        }

    }
}
