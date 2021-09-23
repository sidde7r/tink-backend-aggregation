package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import lombok.Getter;
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
                        .setDescription(remittanceInformationUnstructured)
                        .setPending(false)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                entryReference)
                        .setTransactionDates(getTinkTransactionDates());
        return (Transaction) builder.build();
    }

    @JsonIgnore
    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(bookingDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
