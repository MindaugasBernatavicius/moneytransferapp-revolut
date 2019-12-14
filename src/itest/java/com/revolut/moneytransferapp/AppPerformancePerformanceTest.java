package com.revolut.moneytransferapp;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class AppPerformancePerformanceTest {

    private static final String URL = "http://localhost:4567";

    @BeforeEach
    void arrange(){
        App.main(new String[]{});
    }

    @AfterEach
    void teardown(){
        App.stopService();
    }

    @Test
    @Disabled
    public void createAccount__given2ParallelRequests__duplicateAccountNotCreated()
            throws ExecutionException, InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<InputStream> response1 = executor.submit(() -> new URL(URL + "/accounts").openStream());
        Future<InputStream> response2 = executor.submit(() -> new URL(URL + "/accounts").openStream());

        // when
        String resp1 = new BufferedReader(new InputStreamReader(response1.get()))
                .lines().collect(Collectors.joining(""));
        String resp2 = new BufferedReader(new InputStreamReader(response2.get()))
                .lines().collect(Collectors.joining(""));
        executor.shutdown();

        // then
        System.out.println("R1: " + resp1 + "\n" + "R2: " + resp2 );
    }

    @RepeatedTest(10)
    public void transaction__givenFixedAccountAmount__concurrentTransfersDontOverdraw()
            throws ExecutionException, InterruptedException, IOException {
        // given
        int degreeOfParallelism = 12;
        ExecutorService executor = Executors.newFixedThreadPool(degreeOfParallelism);
        List<Future<InputStream>> responses = new ArrayList<>();

        // when
        for (int i = 0; i < degreeOfParallelism; i++)
            responses.add(executor.submit(() -> makeReq("/accounts", "POST")));
        executor.shutdown();

        // then
        for (int i = 0; i < responses.size(); i++){
            // forcing the wait for all threads to complate,
            // ... CountDownLatch could be used instread
            responses.get(i).get();
        }

        String res = new BufferedReader(new InputStreamReader(
                makeReq("/accounts", "GET")))
                    .lines().collect(Collectors.joining(""));

        String expectedResponse = "[" +
                "{\"id\":0,\"balance\":0.01},{\"id\":1,\"balance\":1.01},{\"id\":2,\"balance\":2.01}," +
                "{\"id\":3,\"balance\":0.0},{\"id\":4,\"balance\":0.0},{\"id\":5,\"balance\":0.0}," +
                "{\"id\":6,\"balance\":0.0},{\"id\":7,\"balance\":0.0},{\"id\":8,\"balance\":0.0}," +
                "{\"id\":9,\"balance\":0.0},{\"id\":10,\"balance\":0.0},{\"id\":11,\"balance\":0.0}," +
                "{\"id\":12,\"balance\":0.0},{\"id\":13,\"balance\":0.0},{\"id\":14,\"balance\":0.0}" +
            "]";

        Assertions.assertEquals(expectedResponse, res);

        // teardown
        teardown();
    }

    public InputStream makeReq(String urlPostfix, String reqMethod){
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            url = new URL(URL + urlPostfix);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(reqMethod);
            is = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}
