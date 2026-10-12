
package com.laura.chatbot_ia.exception;

import java.time.Instant;

// Representa el formato común de las respuestas de error de nuestra API.
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Constructor vacío: permite crear el objeto y asignar sus campos después.
    public ApiError() {
    }

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
