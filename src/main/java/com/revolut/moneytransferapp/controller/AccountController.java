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
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            (Request request, Response response) -> {
                Account account = null;
                try {
                    account = accountService.getAccountById(Integer.parseInt(request.params("id")));
                    response.status(200);
                } catch (AccountNotFoundException e){
                    response.status(404); // account was not found
                }
                return new Gson().toJson(account);
            };

    public Route createAccount =
            (Request request, Response response)
                    -> accountService.createAccount();

    public Route updateAccount =
            (Request request, Response response) -> {
                String decodedBody = URLDecoder.decode(request.body(), "UTF-8");
                Account updateObject = new Gson().fromJson(decodedBody, Account.class);
                updateObject.setId(Integer.parseInt(request.params("id")));
                // TODO :: create a validator for Account
                if(updateObject.getBalance() == null) {
                    response.status(422);
                    return "Incorrect body info";
                } else {
                    try {
                        accountService.updateAccount(updateObject);
                        response.status(200); // updated successfully
                        return "Success";
                    } catch(AccountNotFoundException e) {
                        response.status(404); // account was not found
                        return "Account not found";
                    }
                }
            };

    public Route transferMoney =
            (Request request, Response response) -> {
                int benefactorId = Integer.parseInt(request.params("benefactorId"));
                int beneficiaryId = Integer.parseInt(request.params("beneficiaryId"));
                Matcher matcher = Pattern.compile("\\{\"transferAmount\":\"(.*)\"\\}")
                        .matcher(URLDecoder.decode(request.body(), "UTF-8"));
                if(matcher.find()){
                    BigDecimal amount = new BigDecimal(matcher.group(1));
                    try {
                        accountService.transferMoney(benefactorId, beneficiaryId, amount);
                        response.status(200); // updated successfully
                        return "Success";
                    } catch (InvalidTransferException e){
                        response.status(400); //
                        return "Failure, insufficient funds in benefactor account";
                    } catch (AccountNotFoundException e) {
                        response.status(404); // account was not found
                        return "Failure, insufficient funds in benefactor account";
                    }
                } else {
                    response.status(400);
                    return "Failure";
                }
            };
}
