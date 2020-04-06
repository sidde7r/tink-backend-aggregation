package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {
    private long date;
    private String text;
    private BigDecimal amountHolderCurrency;
    private String merchant;
    private long debitDate;
    private boolean definitive;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(text)
                .setDate(Instant.ofEpochMilli(date).atZone(ZoneId.of("CET")).toLocalDate())
                .setAmount(ExactCurrencyAmount.of(amountHolderCurrency.negate(), Accounts.CURRENCY))
                .build();
    }
}
