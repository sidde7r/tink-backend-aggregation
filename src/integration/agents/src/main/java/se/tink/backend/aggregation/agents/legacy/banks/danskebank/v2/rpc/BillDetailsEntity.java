package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BillDetailsEntity {
    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("Date")
    private String date;

    @JsonProperty("FromAccountId")
    private String fromAccountId;

    @JsonProperty("FromAccountName")
    private String fromAccountName;

    @JsonProperty("FromAccountText")
    private String fromAccountText;

    @JsonProperty("ReceiverName")
    private String receiverName;

    @JsonProperty("ReceiverText")
    private String receiverText;

    @JsonProperty("Reference")
    private String reference;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromAccountName() {
        return fromAccountName;
    }

    public void setFromAccountName(String fromAccountName) {
        this.fromAccountName = fromAccountName;
    }

    public String getFromAccountText() {
        return fromAccountText;
    }

    public void setFromAccountText(String fromAccountText) {
        this.fromAccountText = fromAccountText;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverText() {
        return receiverText;
    }

    public void setReceiverText(String receiverText) {
        this.receiverText = receiverText;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
