package com.revolut.moneytransferapp;

import com.revolut.moneytransferapp.controller.AccountController;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.AccountService;

import static spark.Spark.*;

public class App {

    private AccountRepository accountRepository;
    private AccountService accountService;
    private AccountController accountController;

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
        notFound((req, res) -> "{\"message\":\"Custom 404\"}");
        internalServerError((req, res) -> "{\"message\":\"Custom 500 handling\"}");
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
        post("/accounts", accountController.createAccount);
        get("/accounts", accountController.getAllAccounts);
        get("/accounts/:id", accountController.getAccount);
        put("/accounts/:id", accountController.updateAccount);
        post("/accounts/:benefactorId/transfers/:beneficiaryId", accountController.transferMoney);
    }
}
