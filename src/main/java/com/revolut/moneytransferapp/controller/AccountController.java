package com.revolut.moneytransferapp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.service.AccountService;
import com.revolut.moneytransferapp.service.exceptions.AccountNotFoundException;
import com.revolut.moneytransferapp.service.exceptions.InvalidTransferException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;
import java.util.List;

public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService service) {
        accountService = service;
    }

    public Route getAllAccounts =
            (Request request, Response response)
                    -> new Gson().toJson(accountService
                    .getAccounts(), new TypeToken<List<Account>>(){}.getType());

    public Route getAccount =
            (Request request, Response response)
                    -> new Gson().toJson(accountService
                    .getAccountById(Integer.parseInt(request.params("id"))));

    public Route createAccount =
            (Request request, Response response)
                    -> accountService.createAccount();

    public Route updateAccount =
            (Request request, Response response) -> {
                    Account account = new Gson().fromJson(request.body(), Account.class);
                    try {
                        accountService.updateAccount(Integer.parseInt(request.params("id")), account);
                        response.status(200); // updated successfully
                    } catch(AccountNotFoundException e) {
                        response.status(404); // account was not found
                    };
                    return "";
            };

    public Route transferMoney =
            (Request request, Response response) -> {
                int benefactorId = Integer.parseInt(request.params(request.params("benefactorId")));
                int beneficiaryId = Integer.parseInt(request.params(request.params("beneficiaryId")));
                BigDecimal amount = new BigDecimal(request.params("beneficiaryId"));
                try {
                    accountService.transferMoney(benefactorId, beneficiaryId, amount);
                    response.status(200); // updated successfully
                } catch (InvalidTransferException e){
                    response.status(400); // account was not found
                    return "Failure, insuficient funds in benefactor account";
                }
                return "Success";
    };
}
