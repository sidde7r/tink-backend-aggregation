package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    // Official SDK
    // https://github.com/bunq/sdk_java/blob/develop/src/main/java/com/bunq/sdk/model/generated/endpoint/Payment.java
    @JsonProperty("id")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss.SSSSS")
    private Date created;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss.SSSSS")
    private Date updated;

    @JsonProperty("monetary_account_id")
    private String accountId;

    private AmountEntity amount;
    private String description;
    private String type;
    private TransactionAliasEntity alias;

    @JsonProperty("counterparty_alias")
    private TransactionAliasEntity counterpartyAlias;

    @JsonProperty("sub_type")
    private String subType;

    public String getTransactionId() {
        return transactionId;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getAccountId() {
        return accountId;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public TransactionAliasEntity getAlias() {
        return alias;
    }

    public TransactionAliasEntity getCounterpartyAlias() {
        return counterpartyAlias;
    }

    public String getSubType() {
        return subType;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(created)
                .setAmount(amount.getAsTinkAmount())
                .setDescription(getTinkDescription())
                .build();
    }

    private String getTinkDescription() {
        if (counterpartyAlias != null
                && !Strings.isNullOrEmpty(counterpartyAlias.getDisplayName())) {
            return counterpartyAlias.getDisplayName();
        }
        return description;
    }
}
