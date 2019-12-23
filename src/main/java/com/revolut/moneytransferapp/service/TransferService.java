package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.model.Transfer;
import com.revolut.moneytransferapp.repository.TransferRepository;
import com.revolut.moneytransferapp.repository.repositoryexceptions.OptimisticLockException;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import com.revolut.moneytransferapp.service.serviceexception.InvalidTransferException;
import com.revolut.moneytransferapp.service.serviceexception.TransferNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public class TransferService {

    private AccountService accountService;
    private TransferRepository transferRepository;

    public TransferService(AccountService as, TransferRepository ts) {
        transferRepository = ts;
        accountService = as;
    }

    public List<Transfer> getTransfers() {
        return transferRepository.getAll();
    }

    public Transfer getTransfer(Integer id) throws TransferNotFoundException {
        Transfer transfer = transferRepository.getById(id);
        if (transfer == null) throw new TransferNotFoundException();
        return transfer;
    }

    public synchronized void createTransfer(int benefactorId, int beneficiaryId, BigDecimal amount)
            throws InvalidTransferException, AccountNotFoundException, OptimisticLockException {

        var benefactorAccount = accountService.getAccountById(benefactorId);
        var beneficiaryAccount = accountService.getAccountById(beneficiaryId);

        var benefactorBalance = benefactorAccount.getBalance();
        var beneficiaryBalance = beneficiaryAccount.getBalance();
        //  check if transfer is possible
        if(benefactorBalance.subtract(amount).compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidTransferException("Insufficient balance in benefactors account");

        // transfer
        var updatedBenefactor = new Account(benefactorId, benefactorBalance.subtract(amount));
        accountService.updateAccount(updatedBenefactor);

        var updatedBeneficiary = new Account(beneficiaryId, beneficiaryBalance.add(amount));
        accountService.updateAccount(updatedBeneficiary);

        // if update for the beneficiary is not successful, we need to revert back the benefactor also

        // benefactorAccount.setBalance(benefactorBalance.subtract(amount));
        // beneficiaryAccount.setBalance(beneficiaryBalance.add(amount));

        var transfer = new Transfer(benefactorId, beneficiaryId, amount);
        transferRepository.save(transfer);
    }
}
