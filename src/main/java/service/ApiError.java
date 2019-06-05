package service;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class ApiError {

    private final Date timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    ApiError(HttpStatus status, String message, HttpServletRequest request) {
        this.timestamp = new Date();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = request.getServletPath();
    }

    public Date getTimestamp() {
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