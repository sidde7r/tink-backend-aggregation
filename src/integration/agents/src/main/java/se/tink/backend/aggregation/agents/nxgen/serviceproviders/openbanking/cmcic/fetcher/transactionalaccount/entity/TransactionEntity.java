package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(getDate())
                .setPending(status == TransactionStatusEntity.PDNG)
                .setDescription(String.join(" ", remittanceInformation.getUnstructured()))
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(
                Double.parseDouble(transactionAmount.getAmount()), transactionAmount.getCurrency());
    }

    private LocalDate getDate() {
        return Objects.nonNull(bookingDate) ? bookingDate : transactionDate;
    }
}
