package com.revolut.moneytransferapp.testutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public String getResponseBodyJsonData() throws UnsupportedEncodingException {
        if(responseBodyStream == null){
            return "";
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(responseBodyStream));
            String response = br.lines().collect(Collectors.joining(""));
            if(!response.equals("null")){
                String decodedResponse = URLDecoder.decode(response, "UTF-8");
                Matcher matcher = Pattern.compile("data\":(.+)}").matcher(decodedResponse);
                return matcher.find() ? matcher.group(1) : "";
            } else {
                return "";
            }
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "responseCode=" + responseCode +
                ", headers=" + headers +
                ", responseBodyStream=" + responseBodyStream +
                '}';
    }
}
