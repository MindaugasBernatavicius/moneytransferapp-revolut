package com.revolut.moneytransferapp.model;

import com.google.gson.annotations.Expose;

abstract class VersionedEntity extends Entity {

    @Expose(serialize = false)
    private volatile Integer version;

    public VersionedEntity(Integer id, Integer version) {
        super(id);
        this.version = version;
    }

    public synchronized Integer getVersion() {
        return version;
    }
}
