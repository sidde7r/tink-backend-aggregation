package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception.MandatoryDataMissingException;
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
        return Transaction.builder()
                .setDate(date())
                .setPending(!booked)
                .setAmount(amount())
                .setDescription(title)
                .setPayload(TransactionPayloadTypes.EXTERNAL_ID, transactionId)
                .build();
    }

    private LocalDate date() {
        if (!Strings.isNullOrEmpty(bookingDate)) {
            return LocalDate.parse(bookingDate);
        }
        if (!Strings.isNullOrEmpty(transactionDate)) {
            return LocalDate.parse(transactionDate);
        }
        throw new MandatoryDataMissingException("Cannot parse transaction with no valid date");
    }

    private ExactCurrencyAmount amount() {
        if (!NordeaDkConstants.CURRENCY.equals(currency)) {
            throw new UnsupportedCurrencyException("Cannot parse transaction, wrong currency");
        }
        return ExactCurrencyAmount.of(amount, NordeaDkConstants.CURRENCY);
    }
}
