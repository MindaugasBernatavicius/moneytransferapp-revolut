package com.revolut.moneytransferapp.repository;

import com.revolut.moneytransferapp.repository.repositoryexceptions.OptimisticLockException;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;

import java.util.Collection;

public interface Repository<T> {
    Collection<T> getAll();
    T getById(int id);
    int save(T t);
    void update(T t) throws OptimisticLockException, AccountNotFoundException;
}
