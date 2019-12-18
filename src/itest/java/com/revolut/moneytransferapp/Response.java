package com.revolut.moneytransferapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Response {

    private int responseCode;
    private Map<String, List<String>> headers;
    private InputStream responseBodyStream;

    public Response(int responseCode, Map<String, List<String>> headers, InputStream responseBodyStream) {
        this.responseCode = responseCode;
        this.headers = headers;
        this.responseBodyStream = responseBodyStream;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public InputStream getResponseBodyStream() {
        return responseBodyStream;
    }

    public String getResponseBody(){
        if(responseBodyStream == null){
            return "";
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(responseBodyStream));
            String response = br.lines().collect(Collectors.joining(""));
            return !response.equals("null") ? response : "";
        }
    }
}
