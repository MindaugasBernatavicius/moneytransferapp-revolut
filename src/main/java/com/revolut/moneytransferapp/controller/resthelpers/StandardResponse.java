package com.revolut.moneytransferapp.controller.resthelpers;

import com.google.gson.JsonElement;

public class StandardResponse {
    private ResponseStatus status;
    private String message;
    private JsonElement data;

    public StandardResponse(ResponseStatus status) {
        this.status = status;
    }

    public StandardResponse(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public StandardResponse(ResponseStatus status, JsonElement data) {
        this.status = status;
        this.data = data;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}