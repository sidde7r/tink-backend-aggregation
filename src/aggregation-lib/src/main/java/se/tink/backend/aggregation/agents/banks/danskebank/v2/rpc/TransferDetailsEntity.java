package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferDetailsEntity {
    @JsonProperty("AgreementNumber")
    private String agreementNumber;
    @JsonProperty("Amount")
    private double amount;
    @JsonProperty("Bank")
    private String bank;
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Fee")
    private double fee;
    @JsonProperty("FromAccountId")
    private String fromAccountId;
    @JsonProperty("FromAccountName")
    private String fromAccountName;
    @JsonProperty("FromAccountText")
    private String fromAccountText;
    @JsonProperty("PostingDate")
    private String postingDate;
    @JsonProperty("ToAccountId")
    private String toAccountId;
    @JsonProperty("ToAccountName")
    private String toAccountName;
    @JsonProperty("ToAccountText")
    private String toAccountText;

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public void setAgreementNumber(String agreementNumber) {
        this.agreementNumber = agreementNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
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

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getToAccountName() {
        return toAccountName;
    }

    public void setToAccountName(String toAccountName) {
        this.toAccountName = toAccountName;
    }

    public String getToAccountText() {
        return toAccountText;
    }

    public void setToAccountText(String toAccountText) {
        this.toAccountText = toAccountText;
    }
}
