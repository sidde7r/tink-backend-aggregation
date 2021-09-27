package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
@Getter
@Slf4j
public class TransactionEntity {
    private String archiveId;
    private String message;
    private String reference;
    private String amount;
    private String currency;
    private String bookingDate;
    private String valueDate;
    private String paymentDate;
    private RecipientEntity recipient;
    private String proprietaryTransactionDescription;
    private PayerEntity payer;

    private String getDescription() {
        if (payer != null && StringUtils.isNotBlank(payer.getName())) {
            return payer.getName();
        } else if (recipient != null && StringUtils.isNotBlank(recipient.getName())) {
            return recipient.getName();
        } else if (StringUtils.isNotBlank(message)) {
            return message;
        } else if (StringUtils.isNotBlank(proprietaryTransactionDescription)) {
            return proprietaryTransactionDescription;
        }
        return "";
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setDescription(getDescription())
                    .setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(bookingDate))
                    .setAmount(ExactCurrencyAmount.of(amount, currency))
                    .setTransactionDates(getTransactionDates())
                    .setTransactionReference(reference)
                    .setRawDetails(this)
                    .addExternalSystemIds(
                            TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                            archiveId)
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Parsing error with date");
        }
    }

    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();
        try {
            if (bookingDate != null) {
                builder.setBookingDate(new AvailableDateInformation(LocalDate.parse(bookingDate)));
            }
            if (valueDate != null) {
                builder.setValueDate(new AvailableDateInformation(LocalDate.parse(valueDate)));
            }
            if (paymentDate != null) {
                builder.setExecutionDate(
                        new AvailableDateInformation(LocalDate.parse(paymentDate)));
            }
        } catch (DateTimeParseException e) {
            log.error(
                    "[OP] Date format changed on API side expected format is YYYY-MM-DD, review the logs.",
                    e);
            throw new IllegalStateException("Parsing error with date");
        }

        return builder.build();
    }
}
