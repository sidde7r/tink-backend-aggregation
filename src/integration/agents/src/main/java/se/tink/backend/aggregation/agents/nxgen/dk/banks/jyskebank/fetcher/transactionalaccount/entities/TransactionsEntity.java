package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities;

import java.time.Instant;
import java.time.ZoneId;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {
    private String id;
    private String postingId;
    private String text;
    private DateEntity date;
    private BalanceEntity amount;
    private BalanceEntity originalAmount;
    private BalanceEntity balance;
    private String iconName;
    private boolean balanced;
    private boolean hasAttachment;
    private boolean inSync;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount.getAmount(), amount.getCurrencyCode()))
                .setDescription(text)
                .setDate(
                        Instant.ofEpochMilli(date.getEpoch())
                                .atZone(ZoneId.of("CET"))
                                .toLocalDate())
                .setPending(false)
                .build();
    }
}
