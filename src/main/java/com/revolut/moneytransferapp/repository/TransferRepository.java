package com.revolut.moneytransferapp.repository;

import com.revolut.moneytransferapp.model.Transfer;

import java.util.ArrayList;
import java.util.List;

public class TransferRepository implements Repository<Transfer>{
    private List<Transfer> transfers;
    private int transferCount;

    public TransferRepository() {
        transfers = new ArrayList<>();
        transferCount = transfers.size();
    }

    @Override
    public List<Transfer> getAll() {
        return transfers;
    }

    @Override
    public Transfer getById(int id) {
        for (Transfer transfer : transfers)
            if(transfer.getId() == id)
                return transfer;
        return null;
    }

    @Override
    public int save(Transfer transfer) {
        int id = transfers.size();
        transfer.setId(id);
        transfers.add(transfer);
        ++transferCount;
        return id;
    }

    @Override
    public void update(Transfer transfer) {
        var exceptionMsg = "Transfer object can not be changed";
        throw new UnsupportedOperationException(exceptionMsg);
    }
}
