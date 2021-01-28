package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardTransaction {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    @JsonProperty("transaction_type")
    private String transactionType;

    // Amount of the transaction in local currency
    @JsonProperty private BigDecimal amount;

    @JsonProperty private String currency;

    // The amount of the transaction in the original currency
    @JsonProperty("original_amount")
    private BigDecimal originalAmount;

    @JsonProperty("original_currency")
    private String originalCurrency;

    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;

    @JsonProperty private String title;

    @JsonProperty private boolean booked;

    @JsonProperty("booking_date")
    private LocalDate bookingDate;

    @JsonProperty("merchant_city")
    private String merchantCity;

    @JsonProperty("merchant_country")
    private String merchantCountry;

    @JsonProperty("invoiced_date")
    private LocalDate invoicedDate;

    @JsonProperty("invoice_status")
    private String invoiceStatus;

    @JsonIgnore
    private boolean hasExchangeRateInfo() {
        return (Objects.nonNull(Strings.emptyToNull(originalCurrency))
                && !originalCurrency.equalsIgnoreCase(currency));
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                (Builder)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(amount, currency))
                                .setDate(getDate())
                                .setDescription(title)
                                .setPending(!booked)
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        transactionId);
        if (hasExchangeRateInfo()) {
            builder.setPayload(TransactionPayloadTypes.EXCHANGE_RATE, exchangeRate.toPlainString());
            builder.setPayload(TransactionPayloadTypes.LOCAL_CURRENCY, originalCurrency);
            builder.setPayload(
                    TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY,
                    originalAmount.toPlainString());
        }

        return builder.build();
    }

    private LocalDate getDate() {
        if (booked && Objects.nonNull(bookingDate)) {
            return bookingDate;
        }
        return transactionDate;
    }
}
