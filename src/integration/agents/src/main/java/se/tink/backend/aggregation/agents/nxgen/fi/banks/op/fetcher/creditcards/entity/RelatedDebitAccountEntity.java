package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelatedDebitAccountEntity {

    private String debitAccountNumber;
    private double debitBalance;
    private double amountAvailable;

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public double getDebitBalance() {
        return debitBalance;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }
}
