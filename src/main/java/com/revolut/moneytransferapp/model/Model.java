package com.revolut.moneytransferapp.model;

abstract class Model {
    private Integer id;

    public Model(){};

    public Model(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}