package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates.Builder;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Data
@NoArgsConstructor
public class TransactionsItemEntity {

    private static final String STATUS_BOOKED = "BOOK";
    private static final String STATUS_PENDING = "PDNG";

    private RemittanceInformationEntity remittanceInformation;

    private TransactionAmountEntity transactionAmount;

    private String expectedBookingDate;

    private String bookingDate;

    private String valueDate;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private String entryReference;

    private String status;

    public Transaction toTinkTransactions() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount(creditDebitIndicator))
                .setTransactionReference(entryReference)
                .setPending(isPending())
                .setDate(getTransactionDate())
                .setTransactionDates(getTransactionDates())
                .setDescription(
                        remittanceInformation.getUnstructured().stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator.toString()))
                .build();
    }

    @JsonIgnore
    private LocalDate getTransactionDate() {
        return LocalDate.parse(
                ObjectUtils.firstNonNull(bookingDate, expectedBookingDate, valueDate),
                ISO_OFFSET_DATE_TIME);
    }

    private TransactionDates getTransactionDates() {
        Builder builder = TransactionDates.builder();
        builder.setBookingDate(
                new AvailableDateInformation()
                        .setDate(
                                LocalDate.parse(
                                        expectedBookingDate != null
                                                ? expectedBookingDate
                                                : bookingDate,
                                        ISO_OFFSET_DATE_TIME)));

        Optional.ofNullable(valueDate)
                .ifPresent(
                        date ->
                                builder.setValueDate(
                                        new AvailableDateInformation()
                                                .setDate(
                                                        LocalDate.parse(
                                                                valueDate, ISO_OFFSET_DATE_TIME))));

        return builder.build();
    }

    private boolean isPending() {
        return STATUS_PENDING.equals(status) || !STATUS_BOOKED.equals(status);
    }
}
