package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransEntity {

    private double reservedAmount;
    private long fromDate;
    private long toDate;

    public double getReservedAmount() {
        return reservedAmount;
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }
}
