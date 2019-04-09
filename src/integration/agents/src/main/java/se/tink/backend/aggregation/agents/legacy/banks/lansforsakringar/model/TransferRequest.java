package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TransferRequest {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";

    private String toText;
    private double amount;
    private String bankName;
    private String fromText;
    private String toAccount;
    private String challenge;
    private String response;
    private String fromAccount;

    public String getToText() {
        return toText;
    }

    public void setToText(String toText) {
        this.toText = toText;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getFromText() {
        return fromText;
    }

    public void setFromText(String fromText) {
        this.fromText = fromText;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String calculateHash() {
        return String.valueOf(
                java.util.Objects.hash(
                        getAmountForHash(amount), toText, fromText, bankName, fromAccount));
    }

    private String getAmountForHash(double amount) {
        return new DecimalFormat(
                        FOUR_POINT_PRECISION_FORMAT_STRING,
                        DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }
}
