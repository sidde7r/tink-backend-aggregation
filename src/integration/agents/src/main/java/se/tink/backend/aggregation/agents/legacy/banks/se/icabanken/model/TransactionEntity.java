package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    @JsonProperty("AccountBalance")
    protected double accountBalance;

    @JsonProperty("Amount")
    protected double amount;

    @JsonProperty("PostedDate")
    protected String postedDate;

    @JsonProperty("MemoText")
    protected String memoText;

    @JsonProperty("Type")
    protected String type;

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getMemoText() {
        return memoText;
    }

    public void setMemoText(String memoText) {
        this.memoText = memoText;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public Transaction toTransaction() {
        return toTransaction(false);
    }

    public Transaction toTransaction(boolean reserved) {
        Transaction transaction = new Transaction();

        transaction.setDescription(getDescription());
        transaction.setDate(DateUtils.flattenTime(DateUtils.parseDate(getDate().substring(0, 10))));
        transaction.setAmount(getAmount());
        transaction.setPending(reserved);
        transaction.setType(getTinkTransactionType());

        return transaction;
    }

    public String getDescription() {
        return getMemoText();
    }

    public String getDate() {
        return getPostedDate();
    }

    private TransactionTypes getTinkTransactionType() {
        if (type == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (type.toLowerCase()) {
            case "internationaltransaction":
            case "notdefined":
                return TransactionTypes.DEFAULT;
            case "transfer":
                return TransactionTypes.TRANSFER;
            case "paymentbg":
            case "paymentpg":
            case "payment":
                return TransactionTypes.PAYMENT;
            default:
                return TransactionTypes.DEFAULT;
        }
    }
}
