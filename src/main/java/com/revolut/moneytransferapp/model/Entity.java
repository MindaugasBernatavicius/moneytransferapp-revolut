package com.revolut.moneytransferapp.model;

import com.google.gson.annotations.Expose;

abstract class Entity {

    @Expose(serialize = true)
    private Integer id;

    public Entity(){};
    public Entity(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
}