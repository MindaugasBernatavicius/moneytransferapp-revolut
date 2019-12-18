package com.revolut.moneytransferapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RequestUtil {

    private String baseURL;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public Response makeReq(String urlPostfix, String reqMethod){
        return this.makeReq(urlPostfix, reqMethod, null);
    }

    public Response makeReq(String urlPostfix, String reqMethod, String rawData){
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        Response resp = null;
        try {
            url = new URL(baseURL + urlPostfix);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(reqMethod);
            // send body, if passed in
            if(rawData != null){
                String encodedData = URLEncoder.encode( rawData, "UTF-8" );
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(encodedData.getBytes());
                os.flush();
                os.close();
            }
            // handle response
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST)
                resp = new Response(conn.getResponseCode(), conn.getHeaderFields(), conn.getInputStream());
            else
                resp = new Response(conn.getResponseCode(), conn.getHeaderFields(), conn.getErrorStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }
}
