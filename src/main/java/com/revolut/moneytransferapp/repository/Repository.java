package com.revolut.moneytransferapp.repository;

import java.util.Collection;

public interface Repository<T> {
    Collection<T> getAll();
    T getById(int id);
    int save(T t);
    void update(T t);
}
