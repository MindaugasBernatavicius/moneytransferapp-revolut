package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.repository.TransferRepository;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import com.revolut.moneytransferapp.service.serviceexception.InvalidTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferServiceTest {

    TransferRepository transferRepository;
    TransferService transferService;

    AccountRepository accountRepository;
    AccountService accountService;

    @BeforeEach
    public void initData(){
        accountRepository = new AccountRepository();
        accountRepository.setAccounts(new ArrayList<>());
        accountService = new AccountService(accountRepository);

        transferRepository = new TransferRepository();
        transferService = new TransferService(accountService, transferRepository);
    }

    @Test
    public void createTransfer__givenSufficientAmountInBenefactorsAcc__transfersSuccessfully()
            throws InvalidTransferException, AccountNotFoundException {
        // given
        var benefactorId = 1; var beneficiaryId = 2;
        var benefactorsInitialBalance = new BigDecimal("1");
        var beneficiariesInitialBalance = new BigDecimal("2");
        var accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);

        // when
        var amountToTransfer = new BigDecimal("0.99");
        transferService.createTransfer(benefactorId, beneficiaryId, amountToTransfer);

        // then
        var expectedBenefactorsBalance = benefactorsInitialBalance.subtract(amountToTransfer);
        var expectedBeneficiariesBalance = beneficiariesInitialBalance.add(amountToTransfer);
        assertEquals(expectedBenefactorsBalance, accountService.getAccountById(benefactorId).getBalance());
        assertEquals(expectedBeneficiariesBalance, accountService.getAccountById(beneficiaryId).getBalance());
    }

    @Test
    public void createTransfer__givenInSufficientAmountInBenefactorsAcc__throwsException(){
        // given
        var benefactorId = 1; var beneficiaryId = 2;
        var benefactorsInitialBalance = new BigDecimal("1");
        var beneficiariesInitialBalance = new BigDecimal("2");
        var accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);

        // when / then
        var amountToTransfer = new BigDecimal("1.01");
        assertThrows(InvalidTransferException.class,
                () -> transferService.createTransfer(benefactorId, beneficiaryId, amountToTransfer));
    }

    @Test
    public void createTransfer__givenNonExistingBenefactorsAcc__throwsException(){
        // given
        var benefactorId = 1; var beneficiaryId = 2;
        var beneficiariesInitialBalance = new BigDecimal("2");
        var accounts =  new ArrayList<Account>(){{
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);
        // when / then
        var amountToTransfer = new BigDecimal("1.01");
        assertThrows(AccountNotFoundException.class,
                () -> transferService.createTransfer(benefactorId, beneficiaryId, amountToTransfer));
    }

    @Test
    public void createTransfer__givenNonExistingBeneficiariesAcc__throwsException(){
        // given
        var benefactorId = 1; var beneficiaryId = 2;
        var benefactorsInitialBalance = new BigDecimal("1");
        var accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
        }};
        accountRepository.setAccounts(accounts);
        // when / then
        var amountToTransfer = new BigDecimal("1.01");
        assertThrows(AccountNotFoundException.class,
                () -> transferService.createTransfer(benefactorId, beneficiaryId, amountToTransfer));
    }
}