package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankAccountsEntity {
    private String bankAccountForRealizations;
    private String bankAccountForDividends;
    private String bankAccountForInterest;
    private String bankAccountForCharges;
    private String bankAccountForAsk;

    public String getBankAccountForRealizations() {
        return bankAccountForRealizations;
    }

    public String getBankAccountForDividends() {
        return bankAccountForDividends;
    }

    public String getBankAccountForInterest() {
        return bankAccountForInterest;
    }

    public String getBankAccountForCharges() {
        return bankAccountForCharges;
    }

    public String getBankAccountForAsk() {
        return bankAccountForAsk;
    }
}
