package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class UpcomingTransactionEntity {
    private long id;

    @JsonProperty("account_id")
    private long accountId;

    private double amount;
    private String currency;

    @JsonProperty("preauth_type")
    private String preauthType;

    @JsonProperty("expires_at")
    private Date expiresAt;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("preauth_type_details")
    private UpcomingTransactionDetails details;

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPreauthType() {
        return preauthType;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDescription(details.getMerchantName())
                .setDate(expiresAt)
                .setAmount(new Amount(currency, amount))
                .build();
    }
}
