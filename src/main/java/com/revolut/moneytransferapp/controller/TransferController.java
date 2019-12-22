package com.revolut.moneytransferapp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revolut.moneytransferapp.controller.resthelpers.JsonResponse;
import com.revolut.moneytransferapp.controller.resthelpers.ResponseStatus;
import com.revolut.moneytransferapp.model.Transfer;
import com.revolut.moneytransferapp.service.TransferService;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import com.revolut.moneytransferapp.service.serviceexception.InvalidTransferException;
import com.revolut.moneytransferapp.service.serviceexception.TransferNotFoundException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.util.List;

public class TransferController {

    private TransferService transferService;

    public TransferController(TransferService service){
        transferService = service;
    }

    public Route getAllTransfers = (Request request, Response response) -> {
        var transfers = transferService.getTransfers();
        var token = new TypeToken<List<Transfer>>(){}.getType();
        var responseData = new Gson().toJsonTree(transfers, token);
        var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, responseData);
        return new Gson().toJson(jsonResponse);
    };

    public Route getTransfer = (Request request, Response response) -> {
        var transferId = Integer.parseInt(request.params("id"));
        try {
            var account = transferService.getTransfer(transferId);
            response.status(200);
            var responseData = new Gson().toJsonTree(account);
            var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, responseData);
            return new Gson().toJson(jsonResponse);
        } catch (TransferNotFoundException e){
            response.status(404);
            var respString = "Transfer not found";
            var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
            return new Gson().toJson(jsonResponse);
        }
    };

    public Route createTransfer = (Request request, Response response) -> {
        try {
            var decodedBody = URLDecoder.decode(request.body(), "UTF-8");
            var t = new Gson().fromJson(decodedBody, Transfer.class);
            transferService.createTransfer(t.getBenefactorId(), t.getBeneficiaryId(), t.getAmount());
            response.status(200);
            var respString = "Transfer successful";
            var jsonResponse = new JsonResponse(ResponseStatus.SUCCESS, respString);
            return new Gson().toJson(jsonResponse);
        } catch (InvalidTransferException e) {
            response.status(400);
            var jsonResponse = new JsonResponse(ResponseStatus.ERROR, e.getMessage());
            return new Gson().toJson(jsonResponse);
        } catch (AccountNotFoundException e) {
            response.status(404);
            var respString = "Account not found";
            var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
            return new Gson().toJson(jsonResponse);
        } catch (NullPointerException e){
            response.status(422);
            var respString = "Incorrect request body";
            var jsonResponse = new JsonResponse(ResponseStatus.ERROR, respString);
            return new Gson().toJson(jsonResponse);
        }
    };
}
