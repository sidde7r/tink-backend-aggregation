package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.common.entity.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
public class TransactionEntity {
    private String entryReference;
    private AmountEntity transactionAmount;
    private CreditDebitIndicator creditDebitIndicator;
    private TransactionStatus status;
    private OffsetDateTime bookingDate;
    private OffsetDateTime valueDate;
    private OffsetDateTime transactionDate;
    private List<String> remittanceInformation;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkTransactionsAmount(creditDebitIndicator))
                .setDate(getDate())
                .setTransactionDates(getTransactionDates())
                .setDescription(getDescription(remittanceInformation))
                .setPending(!TransactionStatus.BOOK.equals(status))
                .build();
    }

    @JsonIgnore
    private LocalDate getDate() {
        return bookingDate != null ? bookingDate.toLocalDate() : transactionDate.toLocalDate();
    }

    @JsonIgnore
    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();
        Optional.ofNullable(valueDate)
                .ifPresent(
                        date ->
                                builder.setValueDate(
                                        new AvailableDateInformation(valueDate.toLocalDate())
                                                .setInstant(valueDate.toInstant())));
        Optional.ofNullable(bookingDate)
                .ifPresent(
                        date ->
                                builder.setBookingDate(
                                        new AvailableDateInformation(bookingDate.toLocalDate())
                                                .setInstant(bookingDate.toInstant())));
        Optional.ofNullable(transactionDate)
                .ifPresent(
                        date ->
                                builder.setExecutionDate(
                                        new AvailableDateInformation(transactionDate.toLocalDate())
                                                .setInstant(transactionDate.toInstant())));
        return builder.build();
    }

    @JsonIgnore
    private String getDescription(List<String> remittanceInformation) {
        return remittanceInformation.isEmpty()
                ? ""
                : remittanceInformation.stream()
                        .filter(Objects::nonNull)
                        .filter(text -> !text.equals("NOTPROVIDED"))
                        .map(text -> text.replaceAll("\\s\\s", ""))
                        .collect(Collectors.joining(", "));
    }
}
