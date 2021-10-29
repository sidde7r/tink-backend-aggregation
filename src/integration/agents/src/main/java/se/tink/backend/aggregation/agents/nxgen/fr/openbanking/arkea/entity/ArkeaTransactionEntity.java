package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@Data
@JsonObject
@Slf4j
public class ArkeaTransactionEntity {

    private static final String STATUS_BOOKED = "BOOK";
    private static final String STATUS_PENDING = "PDNG";
    private static final String STATUS_OTHER = "OTHR";

    private String resourceId;
    private String entryReference;
    private ArkeaTransactionAmountEntity transactionAmount;
    private String creditDebitIndicator;
    private String status;
    private String bookingDate;
    private String valueDate;
    private String transactionDate;
    private ArkeaRemittanceInformationEntity remittanceInformation;
    private String additionalTransactionInformation;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount(creditDebitIndicator))
                .setPending(isPendingStatus())
                .setDate(getDate())
                .setTransactionDates(getTransactionDates())
                .setDescription(getDescription())
                .build();
    }

    private boolean isPendingStatus() {
        if (STATUS_OTHER.equals(status)) {
            log.warn("Transaction of OTHR status");
        }
        return STATUS_PENDING.equals(status);
    }

    private String getDescription() {
        return remittanceInformation.getRemittanceInformationUnstructuredList().stream()
                .collect(Collectors.joining(", "));
    }

    private LocalDate getDate() {
        return LocalDate.parse(
                ObjectUtils.firstNonNull(bookingDate, valueDate, transactionDate),
                ISO_LOCAL_DATE_TIME);
    }

    private TransactionDates getTransactionDates() {
        return TransactionDates.builder()
                .setBookingDate(
                        new AvailableDateInformation()
                                .setDate(LocalDate.parse(bookingDate, ISO_LOCAL_DATE_TIME)))
                .setValueDate(
                        new AvailableDateInformation()
                                .setDate(LocalDate.parse(valueDate, ISO_LOCAL_DATE_TIME)))
                .setExecutionDate(
                        new AvailableDateInformation()
                                .setDate(LocalDate.parse(transactionDate, ISO_LOCAL_DATE_TIME)))
                .build();
    }
}
