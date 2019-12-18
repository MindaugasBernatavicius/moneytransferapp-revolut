package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.exceptions.AccountNotFoundException;
import com.revolut.moneytransferapp.service.exceptions.InvalidTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTest {

    AccountRepository accountRepository;
    AccountService accountService;

    @BeforeEach
    public void initData(){
        accountRepository = new AccountRepository();
        accountRepository.setAccounts(new ArrayList<>());
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void getAccounts__whenCalled__thenReturnsAllAccounts(){
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        List<Account> accountsOut = accountService.getAccounts();

        // then
        assertEquals(accountsIn, accountsOut);
    }

    @Test
    public void getAccountById__whenCalledWithExistingId__thenReturnsAccountForThatId()
            throws AccountNotFoundException {
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        Account accountOut = accountService.getAccountById(1);

        // then
        assertEquals(accountsIn.get(0), accountOut);
    }

    @Test
    public void getAccountById__whenCalledWithNonExistingId__thenThrows() {
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when / then
        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountById(3));
    }

    @Test
    public void createAccount__whenCalled__thenCreatesAccountReturnsId(){
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1)); }};
        accountRepository.setAccounts(accountsIn);

        // when
        int createdAccountId = accountService.createAccount();

        // then
        assertEquals(2, createdAccountId);
    }

    @Test
    public void updateAccount__whenNoAccountsPresent__throwsException(){
        // given
        int accId = 1; BigDecimal balance = new BigDecimal(1);
        Account account = new Account(accId, balance);

        // when / then
        assertThrows(AccountNotFoundException.class,
                () -> accountService.updateAccount(accId, account));
    }

    @Test
    public void updateAccount__givenExistingAccountId__updatesAccCorrectly()
            throws AccountNotFoundException {
        // given
        int accId = 1; BigDecimal balance = new BigDecimal("1");
        List<Account> accounts =  new ArrayList<Account>(){{
            add(new Account(accId, balance)); }};
        accountRepository.setAccounts(accounts);

        // when
        BigDecimal updatedBalance = new BigDecimal("5");
        Account updatedAccount = new Account(accId, updatedBalance);
        accountService.updateAccount(accId, updatedAccount);

        // then
        BigDecimal actualBalance = accountService.getAccountById(accId).getBalance();
        assertEquals(updatedBalance, actualBalance);
    }

    // TODO :: make this a parametrized test
    @Test
    public void transferMoney__givenSufficientAmountInBenefactorsAcc__transfersSuccessfully()
            throws InvalidTransferException, AccountNotFoundException {
        // given
        int benefactorId = 1; int beneficiaryId = 2;
        BigDecimal benefactorsInitialBalance = new BigDecimal("1");
        BigDecimal beneficiariesInitialBalance = new BigDecimal("2");
        List<Account> accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);

        // when
        BigDecimal amountToTransfer = new BigDecimal("0.99");
        accountService.transferMoney(benefactorId, beneficiaryId, amountToTransfer);

        // then
        BigDecimal expectedBenefactorsBalance = benefactorsInitialBalance.subtract(amountToTransfer);
        BigDecimal expectedBeneficiariesBalance = beneficiariesInitialBalance.add(amountToTransfer);
        assertEquals(expectedBenefactorsBalance, accountService.getAccountById(benefactorId).getBalance());
        assertEquals(expectedBeneficiariesBalance, accountService.getAccountById(beneficiaryId).getBalance());
    }

    @Test
    public void transferMoney__givenInSufficientAmountInBenefactorsAcc__throwsException(){
        // given
        int benefactorId = 1; int beneficiaryId = 2;
        BigDecimal benefactorsInitialBalance = new BigDecimal("1");
        BigDecimal beneficiariesInitialBalance = new BigDecimal("2");
        List<Account> accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);

        // when / then
        BigDecimal amountToTransfer = new BigDecimal("1.01");
        assertThrows(InvalidTransferException.class,() ->
                accountService.transferMoney(benefactorId, beneficiaryId, amountToTransfer));
    }

    @Test
    public void transferMoney__givenNonExistingBenefactorsAcc__throwsException(){
        // given
        int benefactorId = 1; int beneficiaryId = 2;
        BigDecimal beneficiariesInitialBalance = new BigDecimal("2");
        List<Account> accounts =  new ArrayList<Account>(){{
            add(new Account(beneficiaryId, beneficiariesInitialBalance));
        }};
        accountRepository.setAccounts(accounts);
        // when / then
        BigDecimal amountToTransfer = new BigDecimal("1.01");
        assertThrows(AccountNotFoundException.class,() ->
                accountService.transferMoney(benefactorId, beneficiaryId, amountToTransfer));
    }

    @Test
    public void transferMoney__givenNonExistingBeneficiariesAcc__throwsException(){
        // given
        int benefactorId = 1; int beneficiaryId = 2;
        BigDecimal benefactorsInitialBalance = new BigDecimal("1");
        List<Account> accounts =  new ArrayList<Account>(){{
            add(new Account(benefactorId, benefactorsInitialBalance));
        }};
        accountRepository.setAccounts(accounts);
        // when / then
        BigDecimal amountToTransfer = new BigDecimal("1.01");
        assertThrows(AccountNotFoundException.class,() ->
                accountService.transferMoney(benefactorId, beneficiaryId, amountToTransfer));
    }
}
