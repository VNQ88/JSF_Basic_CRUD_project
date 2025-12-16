package com.andrew.controller;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class HelloWorld implements Serializable {

    private static final long serialVersionUID = 1L;
    private String message;

    @PostConstruct
    public void init() {
        message = "Hello World";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
