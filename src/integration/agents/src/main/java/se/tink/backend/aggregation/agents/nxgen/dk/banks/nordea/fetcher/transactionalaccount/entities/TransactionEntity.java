package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
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
    private String interestDate;

    public Transaction toTinkTransaction() {
        Transaction.Builder builder =
                Transaction.builder()
                        .setAmount(getAmount())
                        .setDescription(description)
                        .setPending(!booked)
                        .setPayload(TransactionPayloadTypes.EXTERNAL_ID, reconciliationId);
        dateFromTransaction().ifPresent(builder::setDate);
        return builder.build();
    }

    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    private Optional<LocalDate> dateFromTransaction() {
        return Stream.of(bookingDate, transactionDate, interestDate)
                .filter(Objects::nonNull)
                .findFirst()
                .map(LocalDate::parse);
    }
}
