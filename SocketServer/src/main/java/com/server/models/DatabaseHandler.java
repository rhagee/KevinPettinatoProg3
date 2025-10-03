package com.server.models;

public enum DatabaseHandler {
    INSTANCE;

    private boolean isInitialized = false;

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
