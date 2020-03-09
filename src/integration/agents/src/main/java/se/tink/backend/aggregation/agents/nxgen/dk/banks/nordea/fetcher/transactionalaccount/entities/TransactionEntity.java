package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception.MandatoryDataMissingException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {

    private double amount;
    private boolean booked;
    private String bookingDate;
    private String currency;
    private String description;
    private String reconciliationId;
    private String transactionDate;

    public AggregationTransaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDescription(description)
                .setPending(!booked)
                .setDate(getDate())
                .setPayload(TransactionPayloadTypes.EXTERNAL_ID, reconciliationId)
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    private LocalDate getDate() {
        if (bookingDate != null) {
            return LocalDate.parse(bookingDate);
        } else if (transactionDate != null) {
            return LocalDate.parse(transactionDate);
        }
        throw new MandatoryDataMissingException("Unable to parse transaction, date unavailable");
    }
}
