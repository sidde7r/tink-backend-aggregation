package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates.Builder;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateOffsetDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
public class TransactionEntity {
    private String resourceId;

    private AmountTypeEntity transactionAmount;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private TransactionStatusEntity status;

    @JsonDeserialize(using = LocalDateOffsetDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateOffsetDeserializer.class)
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateOffsetDeserializer.class)
    private LocalDate expectedBookingDate;

    @JsonDeserialize(using = LocalDateOffsetDeserializer.class)
    private LocalDate transactionDate;

    private RemittanceInformationEntity remittanceInformation;

    public Transaction toTinkTransaction() {
        boolean pending = isPending();

        return Transaction.builder()
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, resourceId)
                .setTransactionDates(getTransactionDates())
                .setAmount(getAmount())
                .setDate(getDate(pending))
                .setPending(pending)
                .setDescription(String.join(" ", remittanceInformation.getUnstructured()))
                .setType(getTransactionTypes())
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(
                Double.parseDouble(transactionAmount.getAmount()), transactionAmount.getCurrency());
    }

    private LocalDate getDate(boolean pending) {
        if (pending) {
            return ObjectUtils.firstNonNull(expectedBookingDate, transactionDate, valueDate);
        }
        return Optional.ofNullable(bookingDate).orElse(transactionDate);
    }

    private TransactionTypes getTransactionTypes() {
        if (Objects.isNull(remittanceInformation)
                || CollectionUtils.isEmpty(remittanceInformation.getUnstructured())) {
            return TransactionTypes.DEFAULT;
        }

        final String description = remittanceInformation.getUnstructured().get(0).trim();

        if (StringUtils.isBlank(description)) {
            return TransactionTypes.DEFAULT;
        }

        if (description.startsWith("PRLV")) {
            return TransactionTypes.PAYMENT;
        }

        if (description.startsWith("VIR")) {
            return TransactionTypes.TRANSFER;
        }

        if (description.startsWith("RETRAIT")) {
            return TransactionTypes.WITHDRAWAL;
        }

        if (description.contains("PAIEMENT")) {
            return TransactionTypes.CREDIT_CARD;
        }

        return TransactionTypes.DEFAULT;
    }

    @JsonIgnore
    private boolean isPending() {
        return status == TransactionStatusEntity.PDNG || status == TransactionStatusEntity.OTHR;
    }

    @JsonIgnore
    private TransactionDates getTransactionDates() {
        Builder builder = TransactionDates.builder();
        Optional.ofNullable(valueDate)
                .ifPresent(date -> builder.setValueDate(new AvailableDateInformation(valueDate)));
        Optional.ofNullable(bookingDate)
                .ifPresent(
                        date -> builder.setBookingDate(new AvailableDateInformation(bookingDate)));

        return builder.build();
    }
}
