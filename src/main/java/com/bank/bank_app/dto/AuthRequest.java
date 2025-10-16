package com.bank.bank_app.dto;

import java.math.BigDecimal;

public class AuthRequest {
    private String username;
    private String password;
    private BigDecimal balance;

    public AuthRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}