package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Data;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
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
        Builder builder =
                Transaction.builder()
                        .setAmount(getTinkAmount())
                        .setTransactionDates(getTransactionDates())
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
    private Builder setDateOnBuilder(Builder builder) throws BankServiceException {
        return Stream.of(bookingDate, transactionDate, valueDate)
                .filter(Objects::nonNull)
                .findFirst()
                .map(builder::setDate)
                .orElseThrow(
                        () ->
                                new BankServiceException(
                                        BankServiceError.valueOf("No transaction date avaliable")));
    }

    @JsonIgnore
    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();
        Optional.ofNullable(valueDate)
                .ifPresent(date -> builder.setValueDate(new AvailableDateInformation(valueDate)));
        Optional.ofNullable(bookingDate)
                .ifPresent(
                        date -> builder.setBookingDate(new AvailableDateInformation(bookingDate)));
        return builder.build();
    }
}
