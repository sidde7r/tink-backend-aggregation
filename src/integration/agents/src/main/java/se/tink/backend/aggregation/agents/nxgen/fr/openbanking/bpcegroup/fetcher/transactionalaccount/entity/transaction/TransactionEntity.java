package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
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

    private LocalDate bookingDate;

    private LocalDate valueDate;

    private LocalDate transactionDate;

    public Transaction toTinkTransaction() {
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation().setDate(valueDate))
                        .build();

        Builder builder =
                Transaction.builder()
                        .setAmount(getTinkAmount())
                        .setTransactionDates(transactionDates)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                resourceId)
                        .setDescription(String.join(", ", remittanceInformation))
                        .setPending(status != TransactionStatus.BOOK);
        return setDateOnBuilder(builder).build();
    }

    private ExactCurrencyAmount getTinkAmount() {
        final ExactCurrencyAmount tinkAmount = transactionAmount.toTinkAmount();
        return creditDebitIndicator == CreditDebitIndicator.DBIT ? tinkAmount.negate() : tinkAmount;
    }

    @JsonIgnore
    private Builder setDateOnBuilder(Builder builder) {
        return Stream.of(bookingDate, transactionDate, valueDate)
                .filter(Objects::nonNull)
                .findFirst()
                .map(builder::setDate)
                .orElse(builder);
    }
}
