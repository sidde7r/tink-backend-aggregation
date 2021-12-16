package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerMarketUtil;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {
    private String transactionId;

    private boolean hasDetails;

    private boolean booked;

    private BigDecimal amount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    private String interestDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private String title;

    private String description;

    private BigDecimal balanceAfter;

    private TransactionTypeEntity transactionType;

    private String reconciliationStatus;

    private ExchangeEntity exchange;

    public Optional<Transaction> toTinkTransaction(String market) {
        // Sometimes pending transactions have no date
        if (getDate() == null) {
            log.warn("Ignoring transaction with no date.");
            return Optional.empty();
        }

        if (Strings.isNullOrEmpty(currency) || amount == null) {
            log.warn("Ignoring transaction with no currency or amount.");
            return Optional.empty();
        }

        Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDate(getDate())
                        .setDescription(getTransactionDescription(market))
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

        return Optional.of(builder.build());
    }

    @JsonIgnore
    private String getTransactionDescription(String market) {
        if (title != null) {
            return title;
        } else if (NordeaPartnerMarketUtil.isNorway(market) && description != null) {
            return description;
        } else if (transactionType != null && transactionType.getTransactionCodeText() != null) {
            return transactionType.getTransactionCodeText();
        }
        return NordeaPartnerMarketUtil.getLocaleDescription(market);
    }

    @JsonIgnore
    public LocalDate getDate() {
        if (transactionDate != null) {
            return transactionDate;
        }
        return bookingDate;
    }
}
