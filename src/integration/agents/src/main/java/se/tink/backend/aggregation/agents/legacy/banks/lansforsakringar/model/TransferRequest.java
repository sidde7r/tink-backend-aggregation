package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;

@JsonObject
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

    private TransferRequest(
            String bankName,
            double amount,
            TransferMessageFormatter.Messages formattedMessages,
            String fromAccount,
            String toAccount) {
        this.bankName = bankName;
        this.amount = amount;
        this.toText = formattedMessages.getDestinationMessage();
        this.fromText = formattedMessages.getSourceMessage();
        this.challenge = "";
        this.response = "";
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public static TransferRequest create(
            String bankName,
            double amount,
            TransferMessageFormatter.Messages formattedMessages,
            String fromAccount,
            String toAccount) {
        return new TransferRequest(bankName, amount, formattedMessages, fromAccount, toAccount);
    }

    public String getBankName() {
        return bankName;
    }

    public String getFromAccount() {
        return fromAccount;
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
