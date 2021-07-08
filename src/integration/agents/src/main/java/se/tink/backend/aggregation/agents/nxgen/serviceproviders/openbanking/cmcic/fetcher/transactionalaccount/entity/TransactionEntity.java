package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
public class TransactionEntity {
    private String resourceId;

    private AmountTypeEntity transactionAmount;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private TransactionStatusEntity status;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private RemittanceInformationEntity remittanceInformation;

    public Transaction toTinkTransaction() {
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation(valueDate))
                        .build();

        return Transaction.builder()
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, resourceId)
                .setTransactionDates(transactionDates)
                .setAmount(getAmount())
                .setDate(getDate())
                .setPending(status == TransactionStatusEntity.PDNG)
                .setDescription(String.join(" ", remittanceInformation.getUnstructured()))
                .setType(getTransactionTypes())
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(
                Double.parseDouble(transactionAmount.getAmount()), transactionAmount.getCurrency());
    }

    private LocalDate getDate() {
        return Objects.nonNull(bookingDate) ? bookingDate : transactionDate;
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
}
