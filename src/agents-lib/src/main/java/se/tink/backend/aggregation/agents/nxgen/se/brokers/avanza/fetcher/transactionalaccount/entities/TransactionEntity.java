package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {
    private String transactionType;
    private double amount;
    private String description;
    private String currency;
    private String id;

    @JsonProperty("account")
    private AccountInfoEntity accountInfo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date verificationDate;

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public AccountInfoEntity getAccountInfo() {
        return accountInfo;
    }

    public Date getVerificationDate() {
        return verificationDate;
    }

    @JsonIgnore
    public boolean isDepositOrWithdraw() {
        final String type = getTransactionType().toLowerCase();

        return Objects.equals(type, TransactionTypes.DEPOSIT)
                || Objects.equals(type, TransactionTypes.WITHDRAW);
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(verificationDate)
                .setDescription(description)
                .setExternalId(id)
                .setPending(false)
                .build();
    }
}
