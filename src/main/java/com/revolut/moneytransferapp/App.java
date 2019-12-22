package com.revolut.moneytransferapp;

import com.revolut.moneytransferapp.controller.AccountController;
import com.revolut.moneytransferapp.controller.TransferController;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.repository.TransferRepository;
import com.revolut.moneytransferapp.service.AccountService;
import com.revolut.moneytransferapp.service.TransferService;

import static spark.Spark.*;

public class App {

    private AccountRepository accountRepository;
    private AccountService accountService;
    private AccountController accountController;

    private TransferRepository transferRepository;
    private TransferService transferService;
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
    }

    private void setupDependencies(){
        this.accountRepository = new AccountRepository();
        this.accountService = new AccountService(accountRepository);
        this.accountController = new AccountController(accountService);

        this.transferRepository = new TransferRepository();
        this.transferService = new TransferService(accountService, transferRepository);
        this.transferController = new TransferController(transferService);
    }

    private void setupRoutes(){
        path("/api/v1", () -> {
            path("/accounts", () -> {
                post("", accountController.createAccount);
                get("", accountController.getAllAccounts);
                get("/:id", accountController.getAccount);
                put("/:id", accountController.updateAccount);
            });
            path("/transfers", () -> {
                post("", transferController.createTransfer);
                get("", transferController.getAllTransfers);
                get("/:id", transferController.getTransfer);
            });
        });
    }
}
