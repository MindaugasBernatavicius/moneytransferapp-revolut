package com.revolut.moneytransferapp;

import com.google.gson.Gson;
import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.testutils.RequestUtil;
import com.revolut.moneytransferapp.testutils.Response;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppIntegrationConcurrentTest {

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

    @RepeatedTest(10) @Order(1)
    public void createAccount__givenParallelRequests__duplicateAccountNotCreated()
            throws ExecutionException, InterruptedException {
        // given

        // ... Hardcoding the degree or parallelism for now due to the fact that
        // ... if different machines are to be used the test would break. This is due
        // ... to the fact that the expected response is fully precomputed. An approach
        // ... to fix this would be to extract the ids from GET /accounts response and
        // ... check if they are sequential (no skips, no duplicates).

        // int degreeOfParallelism = Runtime.getRuntime().availableProcessors() * 3;
        var degreeOfParallelism = 24;
        var executor = Executors.newFixedThreadPool(degreeOfParallelism);
        var responses = new ArrayList<Future<InputStream>>();

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
        var actualResponse = req.makeReq("/accounts", "GET");
        var actualResponseCode = actualResponse.getResponseCode();
        var actualResponseBody = actualResponse.getResponseBody();
        var expectedResponseBody = "{" +
                "\"status\":\"SUCCESS\"," +
                "\"data\":[" +
                    "{\"balance\":0.01,\"id\":0},{\"balance\":1.01,\"id\":1},{\"balance\":2.01,\"id\":2}," +
                    "{\"balance\":0.0,\"id\":3},{\"balance\":0.0,\"id\":4},{\"balance\":0.0,\"id\":5}," +
                    "{\"balance\":0.0,\"id\":6},{\"balance\":0.0,\"id\":7},{\"balance\":0.0,\"id\":8}," +
                    "{\"balance\":0.0,\"id\":9},{\"balance\":0.0,\"id\":10},{\"balance\":0.0,\"id\":11}," +
                    "{\"balance\":0.0,\"id\":12},{\"balance\":0.0,\"id\":13},{\"balance\":0.0,\"id\":14}," +
                    "{\"balance\":0.0,\"id\":15},{\"balance\":0.0,\"id\":16},{\"balance\":0.0,\"id\":17}," +
                    "{\"balance\":0.0,\"id\":18},{\"balance\":0.0,\"id\":19},{\"balance\":0.0,\"id\":20}," +
                    "{\"balance\":0.0,\"id\":21},{\"balance\":0.0,\"id\":22},{\"balance\":0.0,\"id\":23}," +
                    "{\"balance\":0.0,\"id\":24},{\"balance\":0.0,\"id\":25},{\"balance\":0.0,\"id\":26}" +
                "]}";

        assertEquals(200, actualResponseCode);
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @RepeatedTest(10) @Order(2)
    public void transaction__givenFixedAccountAmount__concurrentTransfersDrawFromAccountCorrectly()
            throws ExecutionException, InterruptedException, UnsupportedEncodingException {

        // given - constructing the threadpool
        var degreeOfParallelism = Runtime.getRuntime().availableProcessors() * 3;
        var executor = Executors.newFixedThreadPool(degreeOfParallelism);
        var responses = new ArrayList<Future<InputStream>>();

        // given - preparing the account to have enought money for all parallel transfers
        var benefactorAccId = 1; var beneficieryAccId = 2;
        var requestBody = "{\"balance\":\"" + new BigDecimal("100") + "\"}";
        var resp = req.makeReq("/accounts/" + benefactorAccId, "PUT", requestBody);

        // given - get the initial blance for both accounts before staring the threads
        var benefactorResponseBefore = req.makeReq("/accounts/" + benefactorAccId, "GET");
        var benefactorBefore = new Gson().fromJson(benefactorResponseBefore.getResponseBodyJsonData(), Account.class);
        var benefactorBalanceBefore = benefactorBefore.getBalance();

        var beneficiaryResponseBefore = req.makeReq("/accounts/" + beneficieryAccId, "GET");
        var beneficiaryBefore = new Gson().fromJson(beneficiaryResponseBefore.getResponseBodyJsonData(), Account.class);
        var beneficiaryBalanceBefore = beneficiaryBefore.getBalance();

        // when
        for (int i = 0; i < degreeOfParallelism; i++){
            var reqUrl = "/transfers";
            var reqBody = "{\"benefactor\":" + 1 + ", \"beneficiary\":" + 2 + ", \"amount\":" + 1 + "}";
            responses.add(executor.submit(() -> req.makeReq(reqUrl, "POST", reqBody).getResponseBodyStream()));
        }
        executor.shutdown();

        for (int i = 0; i < responses.size(); i++)
            responses.get(i).get();

        var benefactorResponseAfter = req.makeReq("/accounts/" + benefactorAccId, "GET");
        var benefactorAfter = new Gson().fromJson(benefactorResponseAfter.getResponseBodyJsonData(), Account.class);
        var benefactorBalanceAfter = benefactorAfter.getBalance();

        var beneficiaryResponseAfter = req.makeReq("/accounts/" + beneficieryAccId, "GET");
        var beneficiaryAfter = new Gson().fromJson(beneficiaryResponseAfter.getResponseBodyJsonData(), Account.class);
        var beneficiaryBalanceAfter = beneficiaryAfter.getBalance();

        // then
        var expectedBenefactorBalance = benefactorBalanceBefore
                .subtract(new BigDecimal(Integer.toString(degreeOfParallelism)));
        assertEquals(expectedBenefactorBalance, benefactorBalanceAfter);

        var expectedBeneficiaryBalance = beneficiaryBalanceBefore
                .add(new BigDecimal(Integer.toString(degreeOfParallelism)));
        assertEquals(expectedBeneficiaryBalance, beneficiaryBalanceAfter);
    }
}
