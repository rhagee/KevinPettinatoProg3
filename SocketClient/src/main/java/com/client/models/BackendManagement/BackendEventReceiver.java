package com.client.models.BackendManagement;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import communication.Request;
import communication.Response;
import utils.ResponseCodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BackendEventReceiver implements Runnable {

    private BufferedReader in;
    private final ConcurrentHashMap<String, CompletableFuture<Response<Object>>> pendingRequests = new ConcurrentHashMap<>();

    private String message;

    @Override
    public void run() {
        try {

            //Get the input reader
            this.in = BackendManager.INSTANCE.getIn();

            while (TryReadFromThreadBuffer()) {
                if (Thread.interrupted()) {
                    break;
                }

                //Parse Message into a Response (JSON or Direct Object???) -> Data is in message string

                //MOCK DATA
                Response<Object> response = new Response<>("test", new Object(), ResponseCodes.OK);
                //END OF MOCK DATA

                ResponseCodes code = response.getCode();

                //If it is an error we show it in the UI
                if (code != ResponseCodes.OK && code != ResponseCodes.UPDATE) {
                    AlertManager.get().add("Errore", response.getErrorMessage(), AlertType.ERROR);
                    return;
                }

                //Send it to the right request callback
                if (TrySendToRequest(response)) {
                    return;
                }

                HandleEvent(response);
            }
        } catch (IOException exception) {
            BackendManager.INSTANCE.ConnectionDropped();
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    public boolean TrySendToRequest(Response<Object> response) {
        String id = response.getRequestID();
        if (pendingRequests.containsKey(id)) {
            CompletableFuture<Response<Object>> callback = pendingRequests.remove(id);
            callback.complete(response);
            return true;
        }

        return false;
    }

    public void AddPendingRequest(String id, CompletableFuture<Response<Object>> callback) {
        pendingRequests.put(id, callback);
    }

    private boolean TryReadFromThreadBuffer() throws IOException {
        return (this.message = in.readLine()) != null;
    }

    private void HandleEvent(Response<Object> response) {
        switch (response.getCode()) {
            case ResponseCodes.UPDATE:
                HandleEmailUpdate(response.getPayload());
                break;
            default:
                //Decide if to show a warning in Client UI or just discard
                break;

        }
    }

    private void HandleEmailUpdate(Object emailUpdateObject) {
        //Define the EmailUpdateObject and handle Email Update UI Wise
    }

}
