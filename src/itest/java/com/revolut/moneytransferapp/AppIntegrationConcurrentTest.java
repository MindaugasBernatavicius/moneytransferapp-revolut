package com.revolut.moneytransferapp;

import com.google.gson.Gson;
import com.revolut.moneytransferapp.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppIntegrationConcurrentTest {

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

    @RepeatedTest(10)
    public void createAccount__givenParallelRequests__duplicateAccountNotCreated()
            throws ExecutionException, InterruptedException {
        // given

        // ... Hardcoding the degree or parallelism for now due to the fact that
        // ... if different machines are to be used the test would break. This is due
        // ... to the fact that the expected response is fully precomputed. An approach
        // ... to fix this would be to extract the ids from GET /accounts response and
        // ... check if they are sequential (no skips, no duplicates).

        // int degreeOfParallelism = Runtime.getRuntime().availableProcessors() * 3;
        int degreeOfParallelism = 24;
        ExecutorService executor = Executors.newFixedThreadPool(degreeOfParallelism);
        List<Future<InputStream>> responses = new ArrayList<>();

        // when
        for (int i = 0; i < degreeOfParallelism; i++)
            responses.add(executor.submit(() -> req
                    .makeReq("/accounts", "POST")
                    .getResponseBodyStream()));
        executor.shutdown();

        for (int i = 0; i < responses.size(); i++)
            // forcing wait for threads to complete
            // CountDownLatch could be used instead
            responses.get(i).get();

        // then
        Response actualResponse = req.makeReq("/accounts", "GET");
        int actualResponseCode = actualResponse.getResponseCode();
        String actualResponseBody = actualResponse.getResponseBody();
        String expectedResponseBody = "[" +
                "{\"id\":0,\"balance\":0.01},{\"id\":1,\"balance\":1.01},{\"id\":2,\"balance\":2.01}," +
                "{\"id\":3,\"balance\":0.0},{\"id\":4,\"balance\":0.0},{\"id\":5,\"balance\":0.0}" +
                ",{\"id\":6,\"balance\":0.0},{\"id\":7,\"balance\":0.0},{\"id\":8,\"balance\":0.0}," +
                "{\"id\":9,\"balance\":0.0},{\"id\":10,\"balance\":0.0},{\"id\":11,\"balance\":0.0}," +
                "{\"id\":12,\"balance\":0.0},{\"id\":13,\"balance\":0.0},{\"id\":14,\"balance\":0.0}," +
                "{\"id\":15,\"balance\":0.0},{\"id\":16,\"balance\":0.0},{\"id\":17,\"balance\":0.0}," +
                "{\"id\":18,\"balance\":0.0},{\"id\":19,\"balance\":0.0},{\"id\":20,\"balance\":0.0}," +
                "{\"id\":21,\"balance\":0.0},{\"id\":22,\"balance\":0.0},{\"id\":23,\"balance\":0.0}," +
                "{\"id\":24,\"balance\":0.0},{\"id\":25,\"balance\":0.0},{\"id\":26,\"balance\":0.0}]";

        assertEquals(200, actualResponseCode);
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @RepeatedTest(10)
    public void transaction__givenFixedAccountAmount__concurrentTransfersDrawFromAccountCorrectly()
            throws ExecutionException, InterruptedException {

        // given - constructing the threadpool
        int degreeOfParallelism = Runtime.getRuntime().availableProcessors() * 3;
        ExecutorService executor = Executors.newFixedThreadPool(degreeOfParallelism);
        List<Future<InputStream>> responses = new ArrayList<>();

        // given - preparing the account to have enought money for all parallel transfers
        int benefactorAccId = 1; int beneficieryAccId = 2;
        String requestBody = "{\"balance\":\"" + new BigDecimal("100") + "\"}";
        Response resp = req.makeReq("/accounts/" + benefactorAccId, "PUT", requestBody);

        // given - get the initial blance for both accounts before staring the threads
        Response benefactorResponseBefore = req.makeReq("/accounts/" + benefactorAccId, "GET");
        Account benefactorBefore = new Gson().fromJson(benefactorResponseBefore.getResponseBody(), Account.class);
        BigDecimal benefactorBalanceBefore = benefactorBefore.getBalance();

        Response beneficiaryResponseBefore = req.makeReq("/accounts/" + beneficieryAccId, "GET");
        Account beneficiaryBefore = new Gson().fromJson(beneficiaryResponseBefore.getResponseBody(), Account.class);
        BigDecimal beneficiaryBalanceBefore = beneficiaryBefore.getBalance();

        // when
        for (int i = 0; i < degreeOfParallelism; i++){
            String reqUrl = "/accounts/" + benefactorAccId + "/transfers/" + beneficieryAccId;
            String reqBody = "{\"transferAmount\":\"" + 1 + "\"}";
            responses.add(executor.submit(() -> req.makeReq(reqUrl, "POST", reqBody).getResponseBodyStream()));
        }
        executor.shutdown();

        for (int i = 0; i < responses.size(); i++)
            responses.get(i).get();

        Response benefactorResponseAfter = req.makeReq("/accounts/" + benefactorAccId, "GET");
        Account benefactorAfter = new Gson().fromJson(benefactorResponseAfter.getResponseBody(), Account.class);
        BigDecimal benefactorBalanceAfter = benefactorAfter.getBalance();

        Response beneficiaryResponseAfter = req.makeReq("/accounts/" + beneficieryAccId, "GET");
        Account beneficiaryAfter = new Gson().fromJson(beneficiaryResponseAfter.getResponseBody(), Account.class);
        BigDecimal beneficiaryBalanceAfter = beneficiaryAfter.getBalance();

        // then
        BigDecimal expectedBenefactorBalance = benefactorBalanceBefore
                .subtract(new BigDecimal(Integer.toString(degreeOfParallelism)));
        assertEquals(expectedBenefactorBalance, benefactorBalanceAfter);

        BigDecimal expectedBeneficiaryBalance = beneficiaryBalanceBefore
                .add(new BigDecimal(Integer.toString(degreeOfParallelism)));
        assertEquals(expectedBeneficiaryBalance, beneficiaryBalanceAfter);
    }
}
