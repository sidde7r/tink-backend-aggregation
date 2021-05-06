package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class HandelsbankenSETransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("accountingDate")
    private Date date; // e.g. "2019-10-24"

    private String description; // e.g. "Swish"
    private double transactionAmount; // e.g. -1930.0
    private String transactionAmountFormatted; // e.g. "-1 930,00"

    public Transaction toTinkTransaction() {
        boolean pending = false;

        Matcher matcher =
                HandelsbankenSEConstants.Transactions.PENDING_PATTERN.matcher(description);
        if (matcher.find()) {
            description = matcher.replaceFirst("");
            pending = true;
        }

        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                transactionAmount, HandelsbankenSEConstants.CURRENCY))
                .setDate(date)
                .setDescription(description)
                .setPending(pending)
                .build();
    }
}
