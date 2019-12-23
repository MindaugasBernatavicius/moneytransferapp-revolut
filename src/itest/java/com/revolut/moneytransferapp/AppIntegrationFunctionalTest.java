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

    private static final String URL = "http://localhost:4567/api/v1";
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
        var resp = req.makeReq("/accounts", "GET");
        // then
        var expectedResponse = "{" +
                "\"status\":\"SUCCESS\"," +
                "\"data\":" +
                    "[" +
                        "{\"balance\":0.01,\"id\":0}," +
                        "{\"balance\":1.01,\"id\":1}," +
                        "{\"balance\":2.01,\"id\":2}" +
                    "]" +
                "}";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccount__givenExistingAccountId__returnsCorrespondingAccount(){
        // given
        var accountId = 1;
        // when
        var resp = req.makeReq("/accounts/" + accountId, "GET");
        // then
        var expectedResponse = "{\"status\":\"SUCCESS\",\"data\":{\"balance\":1.01,\"id\":1}}";
        assertEquals(200, resp.getResponseCode());
        assertEquals(expectedResponse, resp.getResponseBody());
    }

    @Test
    public void getAccount__givenNonExistingAccountId__returns404(){
        // given
        var accountId = 50;
        // when
        var resp = req.makeReq("/accounts/" + accountId, "GET");
        // then
        assertEquals(404, resp.getResponseCode());
        assertEquals("{\"status\":\"ERROR\",\"message\":\"Account not found\"}", resp.getResponseBody());
    }

    @Test
    public void postCreateAccount__whenCalled__createsAccountReturnsItsId(){
        // given / when
        var resp = req.makeReq("/accounts", "POST");
        // then
        assertEquals(200, resp.getResponseCode());
        assertEquals("3", resp.getResponseBody());
    }

    @Test
    public void putUpdateAccount__whenExistingAccountIsBeingModified__thenReturns200BalancesIsIncreased()
            throws UnsupportedEncodingException {
        // given
        var createNewAccResp = req.makeReq("/accounts", "POST");
        var createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        var createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        var updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        var createdAccountBalance = updateObject.getBalance();

        // when
        var requestBody = "{\"balance\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        var resp = req.makeReq("/accounts/" + createdAccountsID, "PUT", requestBody);

        // then
        var updatedAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        var updatedAccount = new Gson().fromJson(updatedAccInfo.getResponseBodyJsonData(), Account.class);
        var updatedAccountBalance = updatedAccount.getBalance();
        assertEquals(200, resp.getResponseCode());
        assertEquals("{\"status\":\"SUCCESS\",\"message\":\"Account updated\"}", resp.getResponseBody());
        assertEquals(createdAccountBalance.add(new BigDecimal("1")), updatedAccountBalance);
    }

    @Test
    public void putUpdateAccount__whenExistingAccountModifiedIncorrectData__thenReturns422AndError()
            throws UnsupportedEncodingException {
        // given
        var createNewAccResp = req.makeReq("/accounts", "POST");
        var createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        var createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        var updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        var createdAccountBalance = updateObject.getBalance();

        // when
        var requestBody = "{\"incorrect\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        var resp = req.makeReq("/accounts/" + createdAccountsID, "PUT", requestBody);

        // then
        var updatedAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        var updatedAccount = new Gson().fromJson(updatedAccInfo.getResponseBodyJsonData(), Account.class);
        var updatedAccountBalance = updatedAccount.getBalance();
        assertEquals(422, resp.getResponseCode());
        assertEquals("{\"status\":\"ERROR\",\"message\":\"Incorrect body info\"}", resp.getResponseBody());
        assertEquals(createdAccountBalance, updatedAccountBalance);
    }

    @Test
    public void putUpdateAccount__whenNonExistingAccountIsBeingModified__then404returned()
            throws UnsupportedEncodingException {
        // given
        var createNewAccResp = req.makeReq("/accounts", "POST");
        var createdAccountsID = Integer.parseInt(createNewAccResp.getResponseBody());
        var nonExistingAccount = createdAccountsID + 5000;
        var createdAccInfo = req.makeReq("/accounts/" + createdAccountsID, "GET");
        var updateObject = new Gson().fromJson(createdAccInfo.getResponseBodyJsonData(), Account.class);
        var createdAccountBalance = updateObject.getBalance();

        // when
        var requestBody = "{\"balance\":\"" + createdAccountBalance.add(new BigDecimal("1")) + "\"}";
        var resp = req.makeReq("/accounts/" + nonExistingAccount, "PUT", requestBody);

        // then
        var responseData = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, resp.getResponseCode());
        assertEquals(responseData, resp.getResponseBody());
    }

    @Test
    public void postTransfer__givenIncorrectRequestBody__returns400(){
        // given
        var amountToTransfer = "0.99";
        var accountFrom = 1;
        var accountTo = 2;
        var urlPostfix = "/transfers";
        var requestBody = "{\"amount_x\":\"" + accountFrom
                + "\", \"to_x\":\"" + accountTo
                + "\", \"amount_x\":\"" + amountToTransfer + "\"}";
        // when
        var response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Incorrect request body\"}";
        assertEquals(422, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }

    @Test
    public void postAccountTransfer__givenSufficientAmount__transferSucceeds(){
        // given
        var amountToTransfer = "0.99";
        var accountFrom = 1;
        var accountTo = 2;
        var urlPostfix = "/transfers";
        var requestBody = "{\"benefactor\":" + accountFrom
                        + ", \"beneficiary\":" + accountTo
                        + ", \"amount\":" + amountToTransfer + "}";
        // when
        var response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"SUCCESS\",\"message\":\"Transfer successful\"}";
        assertEquals(200, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }

    @Test
    public void postTransfer__givenInsufficientAmount__transferFails(){
        // given
        var amountToTransfer = "1.99";
        var accountFrom = 1;
        var accountTo = 2;
        var urlPostfix = "/transfers";
        var requestBody = "{\"benefactor\":" + accountFrom
                        + ", \"beneficiary\":" + accountTo
                        + ", \"amount\":" + amountToTransfer + "}";
        // when
        var response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        var expectedResponseBody = "{\"status\":\"ERROR\",\"message\":\"Insufficient balance in benefactors account\"}";
        assertEquals(400, response1.getResponseCode());
        assertEquals(expectedResponseBody, response1.getResponseBody());
    }

    @Test
    public void postTransfer__givenNonExistingBeneficiaryAccount__returns404AndFailure(){
        // given
        var amountToTransfer = "0.99";
        var accountFrom = 1;
        var accountTo = 50;
        var urlPostfix = "/transfers";
        var requestBody = "{\"benefactor\":" + accountFrom
                        + ", \"beneficiary\":" + accountTo
                        + ", \"amount\":" + amountToTransfer + "}";
        // when
        Response response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }

    @Test
    public void postTransfer__givenNonExistingBenefactorAccount__returns404AndFailure(){
        // given
        var amountToTransfer = "0.99";
        var accountFrom = 50;
        var accountTo = 2;
        var urlPostfix = "/transfers";
        var requestBody = "{\"benefactor\":" + accountFrom
                        + ", \"beneficiary\":" + accountTo
                        + ", \"amount\":" + amountToTransfer + "}";
        // when
        var response1 = req.makeReq(urlPostfix, "POST", requestBody);

        // then
        var expectedResponse = "{\"status\":\"ERROR\",\"message\":\"Account not found\"}";
        assertEquals(404, response1.getResponseCode());
        assertEquals(expectedResponse, response1.getResponseBody());
    }
}
