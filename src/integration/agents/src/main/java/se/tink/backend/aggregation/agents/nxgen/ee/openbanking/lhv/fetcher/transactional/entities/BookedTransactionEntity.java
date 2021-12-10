package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Getter
public class BookedTransactionEntity {
    private String entryReference;
    private LocalDate bookingDate;
    private TransactionAmountEntity transactionAmount;
    private String creditorName;
    private AccountNumberEntity creditorAccount;
    private String debtorName;
    private AccountNumberEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.getTinkAmount())
                        .setDate(bookingDate)
                        .setDescription(getDescription())
                        .setPending(false)
                        .setTransactionReference(entryReference)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                entryReference)
                        .setTransactionDates(getTinkTransactionDates());
        return (Transaction) builder.build();
    }

    @JsonIgnore
    private String getDescription() {
        if (transactionAmount.isCredit()) {
            return getDescription(debtorName);
        } else if (transactionAmount.isDebit()) {
            return getDescription(creditorName);
        }
        return remittanceInformationUnstructured != null
                ? remittanceInformationUnstructured
                : StringUtils.EMPTY;
    }

    private String getDescription(String description) {
        return Stream.of(description, remittanceInformationUnstructured)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    @JsonIgnore
    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(bookingDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
