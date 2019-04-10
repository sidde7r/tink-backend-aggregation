package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class TransactionEntity {
    @JsonProperty("Date")
    private String date;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("Text")
    private String text;

    public Optional<Transaction> toTinkTransaction() {
        Date transactionDate = getTransactionDate();
        if (transactionDate == null || Strings.isNullOrEmpty(text)) {
            return Optional.empty();
        }

        return Optional.of(
                Transaction.builder()
                        .setAmount(Amount.inSEK(amount))
                        .setDescription(text)
                        .setDate(getTransactionDate())
                        .build());
    }

    @JsonIgnore
    private Date getTransactionDate() {
        if (Strings.isNullOrEmpty(date)) {
            return null;
        }

        String dateStringWithoutTimeZone = date.substring(0, date.indexOf("+"));
        String dateStringInMillis = dateStringWithoutTimeZone.replaceAll("[^0-9]", "");

        Date date = new Date(Long.parseLong(dateStringInMillis));

        return DateUtils.flattenTime(date);
    }
}
