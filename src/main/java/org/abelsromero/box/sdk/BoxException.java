package org.abelsromero.box.sdk;


import com.mashape.unirest.http.HttpResponse;

/**
 * Created by ABEL.SALGADOROMERO on 29/07/2016.
 */
public class BoxException extends RuntimeException {

    private HttpResponse response;

    public BoxException(String message) {
        super(message);
    }

    public BoxException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    public BoxException(String message, Throwable cause, HttpResponse response) {
        super(message, cause);
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public String getStatusText() {
        return response.getStatusText();
    }
}
