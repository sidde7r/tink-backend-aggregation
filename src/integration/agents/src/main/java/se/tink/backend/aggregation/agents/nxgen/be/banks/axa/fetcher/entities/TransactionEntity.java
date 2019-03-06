package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class TransactionEntity {
    private String accountReference;
    private String amount;
    private String channelCode;
    private String companyCode;
    private String counterparty; // Observed values: empty and non-empty strings
    private String creationTimeStamp;
    private String currency;
    private String date;
    private String detail; // Observed values: Non-empty strings
    private String detailCd;
    private String identificationNumber;
    private String sequenceNumber;
    private String typeCode;
    private String valDt;
    private String verDt;

    public String getAmount() {
        return amount;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public String getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDetails() {
        return detail;
    }
}
