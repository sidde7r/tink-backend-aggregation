package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception.UnsupportedCurrencyException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardTransactionEntity {

    private double amount;
    private boolean booked;
    private String bookingDate;
    private String currency;
    private String title;
    private String transactionDate;
    private String transactionId;

    public Transaction toTinkTransaction() {
        Transaction.Builder builder =
                Transaction.builder()
                        .setPending(!booked)
                        .setAmount(amount())
                        .setDescription(title)
                        .setPayload(TransactionPayloadTypes.EXTERNAL_ID, transactionId);
        dateFromTransaction().ifPresent(builder::setDate);
        return builder.build();
    }

    private Optional<LocalDate> dateFromTransaction() {
        return Stream.of(bookingDate, transactionDate)
                .filter(Objects::nonNull)
                .findFirst()
                .map(LocalDate::parse);
    }

    private ExactCurrencyAmount amount() {
        if (!NordeaDkConstants.CURRENCY.equals(currency)) {
            throw new UnsupportedCurrencyException("Cannot parse transaction, wrong currency");
        }
        return ExactCurrencyAmount.of(amount, NordeaDkConstants.CURRENCY);
    }
}
