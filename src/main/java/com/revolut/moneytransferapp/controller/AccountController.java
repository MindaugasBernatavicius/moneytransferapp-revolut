package com.revolut.moneytransferapp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revolut.moneytransferapp.controller.resthelpers.ResponseStatus;
import com.revolut.moneytransferapp.controller.resthelpers.StandardResponse;
import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.service.AccountService;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import com.revolut.moneytransferapp.service.serviceexception.InvalidTransferException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Pattern;

public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService service) {
        accountService = service;
    }

    public Route getAllAccounts = (Request request, Response response) -> {
                var accounts = accountService.getAccounts();
                var token = new TypeToken<List<Account>>(){}.getType();
                var responseData = new Gson().toJsonTree(accounts, token);
                var standardResponse = new StandardResponse(ResponseStatus.SUCCESS, responseData);
                return new Gson().toJson(standardResponse);
            };

    public Route getAccount = (Request request, Response response) -> {
                int accountId = Integer.parseInt(request.params("id"));
                try {
                    var account = accountService.getAccountById(accountId);
                    response.status(200);
                    var responseData = new Gson().toJsonTree(account);
                    var standardResponse = new StandardResponse(ResponseStatus.SUCCESS, responseData);
                    return new Gson().toJson(standardResponse);
                } catch (AccountNotFoundException e){
                    response.status(404);
                    var respString = "Account not found";
                    var standardResponse = new StandardResponse(ResponseStatus.ERROR, respString);
                    return new Gson().toJson(standardResponse);
                }
            };

    public Route createAccount = (Request request, Response response) -> accountService.createAccount();

    public Route updateAccount = (Request request, Response response) -> {
                var decodedBody = URLDecoder.decode(request.body(), "UTF-8");
                var updateObject = new Gson().fromJson(decodedBody, Account.class);
                updateObject.setId(Integer.parseInt(request.params("id")));
                // TODO :: create a validator for Account
                if(updateObject.getBalance() == null) {
                    response.status(422);
                    var respString = "Incorrect body info";
                    var standardResponse = new StandardResponse(ResponseStatus.ERROR, respString);
                    return new Gson().toJson(standardResponse);
                } else {
                    try {
                        accountService.updateAccount(updateObject);
                        response.status(200);
                        var respString = "Account updated";
                        var standardResponse = new StandardResponse(ResponseStatus.SUCCESS, respString);
                        return new Gson().toJson(standardResponse);
                    } catch(AccountNotFoundException e) {
                        response.status(404);
                        var respString = "Account not found";
                        var standardResponse = new StandardResponse(ResponseStatus.ERROR, respString);
                        return new Gson().toJson(standardResponse);
                    }
                }
            };

    public Route transferMoney = (Request request, Response response) -> {
                int benefactorId = Integer.parseInt(request.params("benefactorId"));
                int beneficiaryId = Integer.parseInt(request.params("beneficiaryId"));
                var pattern = Pattern.compile("\\{\"transferAmount\":\"(.*)\"\\}");
                var matcher = pattern.matcher(URLDecoder.decode(request.body(), "UTF-8"));
                if(matcher.find()){
                    var amount = new BigDecimal(matcher.group(1));
                    try {
                        accountService.transferMoney(benefactorId, beneficiaryId, amount);
                        response.status(200);
                        var respString = "Transfer successful";
                        var standardResponse = new StandardResponse(ResponseStatus.SUCCESS, respString);
                        return new Gson().toJson(standardResponse);
                    } catch (InvalidTransferException e){
                        response.status(400);
                        var standardResponse = new StandardResponse(ResponseStatus.ERROR, e.getMessage());
                        return new Gson().toJson(standardResponse);
                    } catch (AccountNotFoundException e) {
                        response.status(404);
                        var respString = "Account not found";
                        var standardResponse = new StandardResponse(ResponseStatus.ERROR, respString);
                        return new Gson().toJson(standardResponse);
                    }
                } else {
                    response.status(400);
                    var respString = "Incorrect request body";
                    var standardResponse = new StandardResponse(ResponseStatus.ERROR, respString);
                    return new Gson().toJson(standardResponse);
                }
            };
}
