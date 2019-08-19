package se.tink.backend.aggregation.agents.banks.norwegian.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardResponse {
    private double limit;
    private double amountAvailable;
    private double balance;
    private Link cardTransactionsLink;
    private Link paymentsLink;
    private Link invoiceLink;
    private Link limitLink;
    private Link pinLink;
    private Link blockCardLink;
    private Link myBuysLink;
    private Link disputeLink;

    public double getBalance() {
        // include reserved transactions
        return amountAvailable - limit;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }
}

@JsonObject
class Link {
    private String url;
    private String text;
}
