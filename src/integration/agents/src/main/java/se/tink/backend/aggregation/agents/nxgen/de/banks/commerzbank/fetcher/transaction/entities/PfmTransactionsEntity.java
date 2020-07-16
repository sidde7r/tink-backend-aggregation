package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PfmTransactionsEntity {
    private TransactionAmount amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String originalText;

    private boolean uncleared;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount.getValue(), amount.getCurrency()))
                .setDate(date)
                .setDescription(originalText)
                .setPending(uncleared)
                .build();
    }
}
