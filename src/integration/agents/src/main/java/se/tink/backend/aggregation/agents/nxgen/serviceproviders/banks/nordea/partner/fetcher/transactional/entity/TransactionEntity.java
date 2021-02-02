package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {
    private String transactionId;

    private boolean hasDetails;

    private boolean booked;

    private double amount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String interestDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private String title;

    private String description;

    private double balanceAfter;

    private TransactionTypeEntity transactionType;

    private String reconciliationStatus;

    private ExchangeEntity exchange;

    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDate(getDate())
                        .setDescription(getTransactionDescription())
                        .setPending(!booked);

        if (!Strings.isNullOrEmpty(transactionId)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, transactionId);
        }

        if (Objects.nonNull(exchange)) {
            builder.setPayload(TransactionPayloadTypes.EXCHANGE_RATE, exchange.getOriginalRate());
            builder.setPayload(
                    TransactionPayloadTypes.LOCAL_CURRENCY, exchange.getOriginalCurrency());
            builder.setPayload(
                    TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY, exchange.getOriginalAmount());
        }

        return builder.build();
    }

    @JsonIgnore
    private String getTransactionDescription() {
        return description != null ? description : title;
    }

    private Date getDate() {
        if (booked && Objects.nonNull(bookingDate)) {
            return bookingDate;
        }
        return transactionDate;
    }
}
