package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcomingTransactionEntity {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";
    private double amount;
    private Date date;
    private String description;
    private String id;
    private String type;
    private RecipientEntity paymentInfo;
    private TransferInfoEntity transferInfo;
    private String typeAsString;

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public RecipientEntity getPaymentInfo() {
        return paymentInfo;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPaymentInfo(RecipientEntity paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public TransferInfoEntity getTransferInfo() {
        return transferInfo;
    }

    public void setTransferInfo(TransferInfoEntity transferInfo) {
        this.transferInfo = transferInfo;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

    public void setTypeAsString(String typeAsString) {
        this.typeAsString = typeAsString;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setDate(DateUtils.flattenTime(date));
        transaction.setDescription(description);
        transaction.setAmount(amount);

        // All upcoming transactions should be pending.
        transaction.setPending(true);

        return transaction;
    }

    public Transfer toTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(this.amount));
        transfer.setDestinationMessage(this.paymentInfo.getReference());
        transfer.setSourceMessage(this.description);
        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(this.paymentInfo.generalGetAccountIdentifier());
        transfer.setDueDate(this.date);

        return transfer;
    }

    public String calculatePaymentHash(String accountNumber) {
        return String.valueOf(
                java.util.Objects.hash(
                        getAmountForHash(amount),
                        paymentInfo.getGiroNumber(),
                        paymentInfo.getReference(),
                        accountNumber));
    }

    public String calculateTransferHash(String accountNumber) {
        return String.valueOf(
                java.util.Objects.hash(
                        getAmountForHash(amount),
                        transferInfo.getToText(),
                        transferInfo.getFromText(),
                        transferInfo.getToAccountBankName(),
                        accountNumber));
    }

    private String getAmountForHash(double amount) {
        return new DecimalFormat(
                        FOUR_POINT_PRECISION_FORMAT_STRING,
                        DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }
}
