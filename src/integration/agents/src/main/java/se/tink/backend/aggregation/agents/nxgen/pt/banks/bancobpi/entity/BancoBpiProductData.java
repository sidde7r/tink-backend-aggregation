package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BancoBpiProductData {

    private String codeAlfa;
    private String codeFamily;
    private String codeSubFamily;
    private String code;
    private String name;
    private BigDecimal balance;
    private String currencyCode;
    private String number;
    private BigDecimal initialBalance;
    private String owner;
    private LocalDate finalDate;
    private LocalDate initialDate;

    public String getCodeAlfa() {
        return codeAlfa;
    }

    public void setCodeAlfa(String codeAlfa) {
        this.codeAlfa = codeAlfa;
    }

    public String getCodeFamily() {
        return codeFamily;
    }

    public void setCodeFamily(String codeFamily) {
        this.codeFamily = codeFamily;
    }

    public String getCodeSubFamily() {
        return codeSubFamily;
    }

    public void setCodeSubFamily(String codeSubFamily) {
        this.codeSubFamily = codeSubFamily;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDate getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }
}
