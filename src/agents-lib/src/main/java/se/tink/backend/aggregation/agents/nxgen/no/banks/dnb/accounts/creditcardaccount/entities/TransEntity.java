package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransEntity {
    private int openBalance;
    private double closeBalance;
    private double noneInvoicedAmount;
    private double purchaseAmount;
    private int paymentAmount;
    private double reservedAmount;
    private long fromDate;
    private long toDate;
    private String fromDateAsString;
    private String toDateAsString;

    public int getOpenBalance() {
        return openBalance;
    }

    public double getCloseBalance() {
        return closeBalance;
    }

    public double getNoneInvoicedAmount() {
        return noneInvoicedAmount;
    }

    public double getPurchaseAmount() {
        return purchaseAmount;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public String getFromDateAsString() {
        return fromDateAsString;
    }

    public String getToDateAsString() {
        return toDateAsString;
    }
}
