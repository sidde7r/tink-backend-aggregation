package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {

    private String id;
    private Instant created;
    private String description;
    private int amount;
    private String currency;
    private Object merchant;
    private String notes;
    private Object metadata;
    @JsonProperty("account_balance")
    private int accountBalance;
    private Object attachments;
    private String category;
    @JsonProperty("is_load")
    private boolean isLoad;
    private Instant settled;
    @JsonProperty("local_amount")
    private int localAmount;
    @JsonProperty("local_currency")
    private String localCurrency;
    private String updated;
    @JsonProperty("account_id")
    private String accountId;
    private CounterpartyEntity counterparty;
    private String scheme;
    @JsonProperty("dedupe_id")
    private String dedupeId;
    private boolean originator;
    @JsonProperty("include_in_spending")
    private boolean includeInSpending;

    Amount getAmount() {
        return Amount.valueOf(currency, amount, 2);
    }

    @JsonProperty("created")
    public void setCreated(String created) {
        if (Strings.isNullOrEmpty(created)) {
            this.created = null;
        } else {
            this.created = Instant.parse(created);
        }
    }

    @JsonProperty("settled")
    public void setSettled(String settled) {
        if (Strings.isNullOrEmpty(settled)) {
            this.settled = null;
        } else {
            this.settled = Instant.parse(settled);
        }
    }

    public Instant getCreated() {
        return created;
    }

    ZonedDateTime getCreated(ZoneId zoneId) {
        return created.atZone(zoneId);
    }

    public String getId() {
        return id;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDateTime(this.getCreated(MonzoConstants.ZONE_ID))
                .setDescription(description)
                .setAmount(this.getAmount())
                .setPending(settled == null)
                .build();
    }

}
