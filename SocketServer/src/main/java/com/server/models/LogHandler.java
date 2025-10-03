package com.server.models;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    private final StringProperty consoleText = new SimpleStringProperty("");

    public StringProperty getConsoleTextProperty() {
        return consoleText;
    }

    @Override
    public void publish(LogRecord record) {
        String level = record.getLevel().toString();
        String message = record.getMessage();
        String name = record.getLoggerName();
        LocalDateTime date = LocalDateTime.now();
        String formatted = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String line = "[" + level + "] (" + formatted + ") " + name + " - " + message + "\n";
        consoleText.set(line);
    }

    public void AddManually(String text) {
        consoleText.set(text);
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
