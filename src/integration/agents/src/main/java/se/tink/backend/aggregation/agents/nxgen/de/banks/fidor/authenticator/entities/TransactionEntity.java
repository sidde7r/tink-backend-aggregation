package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private String id;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("transaction_type")
    private String transactionType;

    private String subject;
    private double amount;

    @JsonProperty("booking_code")
    private String bookingCode;

    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonProperty("value_date")
    private Date valueDate;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    private String currency;

    @JsonProperty("transaction_type_details")
    private TransactionTypeDetailsEntity transactionTypeDetailsEntity;

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getSubject() {
        return subject;
    }

    public double getAmount() {
        return amount;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionTypeDetailsEntity getTransactionTypeDetailsEntity() {
        return transactionTypeDetailsEntity;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(getCurrency(), getAmount()))
                .setDate(getCreatedAt())
                .setDescription(subject)
                .build();
    }
}
