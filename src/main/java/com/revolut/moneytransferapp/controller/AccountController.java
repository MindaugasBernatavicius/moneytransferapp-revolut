package com.revolut.moneytransferapp.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.revolut.moneytransferapp.controller.resthelpers.JsonResponse;
import com.revolut.moneytransferapp.controller.resthelpers.ResponseStatus;
import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.repositoryexceptions.OptimisticLockException;
import com.revolut.moneytransferapp.service.AccountService;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.util.List;

public class AccountController {

    private AccountService accountService;
    private Gson gson;

    public AccountController(AccountService service) {
        accountService = service;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public Route getAllAccounts = (Request request, Response response) -> {
                var accounts = accountService.getAccounts();
                var token = new TypeToken<List<Account>>(){}.getType();
                var responseData = gson.toJsonTree(accounts, token);
                var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, responseData);
                return new Gson().toJson(jsonResponse);
            };

    public Route getAccount = (Request request, Response response) -> {
                var accountId = Integer.parseInt(request.params("id"));
                try {
                    var account = accountService.getAccountById(accountId);
                    response.status(200);
                    var responseData = gson.toJsonTree(account);
                    var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, responseData);
                    return new Gson().toJson(jsonResponse);
                } catch (AccountNotFoundException e){
                    response.status(404);
                    var respString = "Account not found";
                    var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
                    return new Gson().toJson(jsonResponse);
                }
            };

    public Route createAccount = (Request request, Response response) -> accountService.createAccount();

    public Route updateAccount = (Request request, Response response) -> {
                var decodedBody = URLDecoder.decode(request.body(), "UTF-8");
                var accFromRequest = new Gson().fromJson(decodedBody, Account.class);
                var idToUpdate = Integer.parseInt(request.params("id"));
                accFromRequest.setId(idToUpdate);

                if(accFromRequest.getBalance() == null) {
                    response.status(422);
                    var respString = "Incorrect body info";
                    var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
                    return new Gson().toJson(jsonResponse);
                } else {
                    try {
                        accountService.updateAccount(accFromRequest);
                        response.status(200);
                        var respString = "Account updated";
                        var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, respString);
                        return new Gson().toJson(jsonResponse);
                    } catch(AccountNotFoundException e) {
                        response.status(404);
                        var respString = "Account not found";
                        var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
                        return new Gson().toJson(jsonResponse);
                    } catch (OptimisticLockException e) {
                        response.status(409);
                        var respString = "Information changed during the execution of your request, please retry";
                        var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
                        return new Gson().toJson(jsonResponse);
                    }
                }
            };
}
