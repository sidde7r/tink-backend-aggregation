package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    private String resourceId;

    private List<String> remittanceInformation;

    private AmountEntity transactionAmount;

    private CreditDebitIndicator creditDebitIndicator;

    private String entryReference;

    private TransactionStatus status;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    public Transaction toTinkTransaction() {
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation().setDate(valueDate))
                        .build();

        return Transaction.builder()
                .setAmount(getTinkAmount())
                .setTransactionDates(transactionDates)
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, resourceId)
                .setDate(bookingDate != null ? bookingDate : transactionDate)
                .setDescription(String.join(", ", remittanceInformation))
                .setPending(status != TransactionStatus.BOOK)
                .build();
    }

    private ExactCurrencyAmount getTinkAmount() {
        final ExactCurrencyAmount tinkAmount = transactionAmount.toTinkAmount();
        return creditDebitIndicator == CreditDebitIndicator.DBIT ? tinkAmount.negate() : tinkAmount;
    }
}
