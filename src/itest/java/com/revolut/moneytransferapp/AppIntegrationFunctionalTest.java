package com.revolut.moneytransferapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppIntegrationFunctionalTest {

    private static final String URL = "http://localhost:4567";
    private static final RequestUtil req = new RequestUtil();

    @BeforeAll
    static void arrangeForAll(){
        req.setBaseURL(URL);
    }

    @BeforeEach
    void arrange(){
        App.main(new String[]{});
    }

    @AfterEach
    void teardown(){
        App.stopService();
    }

    @Test
    public void getAccount__givenNoParameters__returnsAllAccounts(){
        // given / when
        Response resp = req.makeReq("/accounts", "GET");

        // then
        String expectedResponse = "[" +
                "{\"id\":0,\"balance\":0.01}," +
                "{\"id\":1,\"balance\":1.01}," +
                "{\"id\":2,\"balance\":2.01}]";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccount__givenExistingAccountId__returnsCorrespondingAccount(){
        // given
        int accountId = 1;

        // when
        Response resp = req.makeReq("/accounts/" + accountId, "GET");

        // then
        String expectedResponse = "{\"id\":1,\"balance\":1.01}";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccount__givenNonExistingAccountId__returns404(){
        // given
        int accountId = 50;

        // when
        Response resp = req.makeReq("/accounts/" + accountId, "GET");

        // then
        assertEquals(404, resp.getResponseCode());
        assertEquals("", resp.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenSufficientAmount__transferSucceeds(){
        // given
        String amountToTransfer = "0.99";
        int accountFrom = 1;
        int accountTo = 2;
        String ulrPostfix = "/accounts/" + accountFrom + "/transfers/" + accountTo;
        String requestBody = "{\"transferAmount\":\"" + amountToTransfer + "\"}";

        // when
        Response response1 = req.makeReq(ulrPostfix, "POST", requestBody);

        // then
        assertEquals(200, response1.getResponseCode());
        assertEquals("Success", response1.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenInsufficientAmount__transferFails(){
        // given
        String amountToTransfer = "1.99";
        String urlPostfix = "/accounts/1/transfers/2";
        String requestBody = "{\"transferAmount\":\"" + amountToTransfer + "\"}";

        // when
        Response response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        String expectedResponseBody = "Failure, insufficient funds in benefactor account";
        assertEquals(400, response1.getResponseCode());
        assertEquals(expectedResponseBody, response1.getResponseBody());
    }
}
