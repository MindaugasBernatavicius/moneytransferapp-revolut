package com.revolut.moneytransferapp;

import com.revolut.moneytransferapp.controller.AccountController;
import com.revolut.moneytransferapp.controller.TransferController;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.AccountService;

import static spark.Spark.*;

public class App {

    private AccountRepository accountRepository;
    private AccountService accountService;
    private AccountController accountController;
    private TransferController transferController;

    public static void main(String[] args) {
        App app = new App();
        app.setupSparkConfig();
        app.setupDependencies();
        app.setupRoutes();
        awaitInitialization();
    }

    public static void stopService(){
        stop();
        awaitStop();
    }

    private void setupSparkConfig(){
        threadPool(10);
        after((req, res) -> res.type("application/json"));
        notFound((req, res) -> "{\"status\":\"ERROR\", \"message\":\"Not found\"}");
        internalServerError((req, res) -> "{\"status\":\"ERROR\", \"message\":\"Server error\"}");
        // exception(YourCustomException.class, (exception, request, response) -> {
        //     // Handle the exception here
        // });
    }

    private void setupDependencies(){
        this.accountRepository = new AccountRepository();
        this.accountService = new AccountService(accountRepository);
        this.accountController = new AccountController(accountService);
    }

    private void setupRoutes(){
        path("/api/v1", () -> {
            // before("/*", (q, a) -> log.info("Received api call"));
            path("/accounts", () -> {
                post("", accountController.createAccount);
                get("", accountController.getAllAccounts);
                get("/:id", accountController.getAccount);
                put("/:id", accountController.updateAccount);
                post("/:benefactorId/transfers/:beneficiaryId", accountController.transferMoney);
            });
            // path("/transfers", () -> {
            //     post("", transferController.toString());
            // });
        });

        // path("/api/v2", () -> {}); // Reserved for v2
    }
}
