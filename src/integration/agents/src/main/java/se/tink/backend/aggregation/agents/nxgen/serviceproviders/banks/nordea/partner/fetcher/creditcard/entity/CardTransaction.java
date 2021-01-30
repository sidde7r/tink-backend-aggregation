package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardTransaction {

    private double amount;

    private boolean booked;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    private String currency;

    private String description;

    private BigDecimal exchangeRate;

    private BigDecimal originalAmount;

    private String originalCurrency;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonIgnore
    private boolean hasExchangeRateInfo() {
        return (Objects.nonNull(Strings.emptyToNull(originalCurrency))
                && !originalCurrency.equalsIgnoreCase(currency));
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDate(getDate())
                        .setDescription(getTransactionDescription())
                        .setPending(!booked);
        if (hasExchangeRateInfo()) {
            builder.setPayload(TransactionPayloadTypes.EXCHANGE_RATE, exchangeRate.toPlainString());
            builder.setPayload(TransactionPayloadTypes.LOCAL_CURRENCY, originalCurrency);
            builder.setPayload(
                    TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY,
                    originalAmount.toPlainString());
        }

        return builder.build();
    }

    @JsonIgnore
    private String getTransactionDescription() {
        return description != null ? description : title;
    }

    private LocalDate getDate() {
        if (booked && Objects.nonNull(bookingDate)) {
            return bookingDate;
        }
        return transactionDate;
    }
}
