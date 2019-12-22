package com.revolut.moneytransferapp;

import com.google.gson.Gson;
import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.testutils.RequestUtil;
import com.revolut.moneytransferapp.testutils.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

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
    public void getAccounts__givenNoParameters__returnsAllAccounts(){
        // given / when
        Response resp = req.makeReq("/accounts", "GET");
        // then
        String expectedResponse = "{" +
                "\"status\":\"SUCCESS\"," +
                "\"data\":" +
                    "[" +
                        "{\"id\":0,\"balance\":0.01}," +
                        "{\"id\":1,\"balance\":1.01}," +
                        "{\"id\":2,\"balance\":2.01}" +
                    "]" +
                "}";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccounts__givenExistingAccountId__returnsCorrespondingAccount(){
        // given
        int accountId = 1;
        // when
        Response resp = req.makeReq("/accounts/" + accountId, "GET");
        // then
        String expectedResponse = "{\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"balance\":1.01}}";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccounts__givenNonExistingAccountId__returns404(){
        // given
        int accountId = 50;
        // when
        Response resp = req.makeReq("/accounts/" + accountId, "GET");
        // then
        assertEquals(404, resp.getResponseCode());
        assertEquals("{\"status\":\"ERROR\",\"message\":\"Account not found\"}", resp.getResponseBody());
    }

    @Test
    public void postCreateAccount__whenCalled__createsAccountReturnsItsId(){
        // given / when
        Response resp = req.makeReq("/accounts", "POST");
        // then
        assertEquals(200, resp.getResponseCode());
        assertEquals("3", resp.getResponseBody());
    }

    @Test
    public void putUpdateAccount__whenExistingAccountIsBeingModified__thenReturns200BalancesIsIncreased()
            throws UnsupportedEncodingException {
        // given
        Response createNewAccResp = req.makeReq("/accounts", "POST");
        int createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        Response createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        Account updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        BigDecimal createdAccountBalance = updateObject.getBalance();

        // when
        String requestBody = "{\"balance\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        Response resp = req.makeReq("/accounts/" + createdAccountsID, "PUT", requestBody);

        // then
        Response updatedAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        Account updatedAccount = new Gson().fromJson(updatedAccInfo.getResponseBodyJsonData(), Account.class);
        BigDecimal updatedAccountBalance = updatedAccount.getBalance();
        assertEquals(200, resp.getResponseCode());
        assertEquals("{\"status\":\"SUCCESS\",\"message\":\"Account updated\"}", resp.getResponseBody());
        assertEquals(createdAccountBalance.add(new BigDecimal("1")), updatedAccountBalance);
    }

    @Test
    public void putUpdateAccount__whenExistingAccountModifiedIncorrectData__thenReturns422AndError()
            throws UnsupportedEncodingException {
        // given
        Response createNewAccResp = req.makeReq("/accounts", "POST");
        int createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        Response createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        Account updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        BigDecimal createdAccountBalance = updateObject.getBalance();

        // when
        String requestBody = "{\"incorrect\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        Response resp = req.makeReq("/accounts/" + createdAccountsID, "PUT", requestBody);

        // then
        Response updatedAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        Account updatedAccount = new Gson().fromJson(updatedAccInfo.getResponseBodyJsonData(), Account.class);
        BigDecimal updatedAccountBalance = updatedAccount.getBalance();
        assertEquals(422, resp.getResponseCode());
        assertEquals("{\"status\":\"ERROR\",\"message\":\"Incorrect body info\"}", resp.getResponseBody());
        assertEquals(createdAccountBalance, updatedAccountBalance);
    }

    @Test
    public void putUpdateAccount__whenNonExistingAccountIsBeingModified__then404returned()
            throws UnsupportedEncodingException {
        // given
        Response createNewAccResp = req.makeReq("/accounts", "POST");
        int createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        int nonExistingAccount = createdAccountsID + 5000;
        Response createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        Account updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        BigDecimal createdAccountBalance = updateObject.getBalance();

        // when
        String requestBody = "{\"balance\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        Response resp = req.makeReq("/accounts/" + nonExistingAccount, "PUT", requestBody);

        // then
        String responseData = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, resp.getResponseCode());
        assertEquals(responseData, resp.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenIncorrectRequestBody__returns400(){
        // given
        String amountToTransfer = "0.99";
        int accountFrom = 1;
        int accountTo = 2;
        String ulrPostfix = "/accounts/" + accountFrom + "/transfers/" + accountTo;
        String requestBody = "{\"xxx\":\"" + amountToTransfer + "\"}";

        // when
        Response response1 = req.makeReq(ulrPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Incorrect request body\"}";
        assertEquals(400, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
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
        var expectedResponse = "{\"status\":\"SUCCESS\",\"message\":\"Transfer successful\"}";
        assertEquals(200, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
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
        String expectedResponseBody = "{\"status\":\"ERROR\",\"message\":\"Insufficient balance in benefactors account\"}";
        assertEquals(400, response1.getResponseCode());
        assertEquals(expectedResponseBody, response1.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenNonExistingBeneficiaryAccount__returns404AndFailure(){
        // given
        String amountToTransfer = "0.99";
        int accountFrom = 1;
        int accountTo = 50;
        String ulrPostfix = "/accounts/" + accountFrom + "/transfers/" + accountTo;
        String requestBody = "{\"transferAmount\":\"" + amountToTransfer + "\"}";

        // when
        Response response1 = req.makeReq(ulrPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenNonExistingBenefactorAccount__returns404AndFailure(){
        // given
        String amountToTransfer = "0.99";
        int accountFrom = 50;
        int accountTo = 2;
        String ulrPostfix = "/accounts/" + accountFrom + "/transfers/" + accountTo;
        String requestBody = "{\"transferAmount\":\"" + amountToTransfer + "\"}";

        // when
        Response response1 = req.makeReq(ulrPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }
}