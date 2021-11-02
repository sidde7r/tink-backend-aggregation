package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Data
@NoArgsConstructor
public class TransactionsItemEntity {

    private static final String STATUS_BOOKED = "BOOK";
    private static final String STATUS_PENDING = "PDNG";

    private RemittanceInformationEntity remittanceInformation;

    private TransactionAmountEntity transactionAmount;

    private Date bookingDate;

    private Date expectingBookingDate;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private String entryReference;

    private String status;

    public Transaction toTinkTransactions() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount(creditDebitIndicator))
                .setTransactionReference(entryReference)
                .setPending(isPending())
                .setDate(getTransactionDate())
                .setDescription(
                        remittanceInformation.getUnstructured().stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator.toString()))
                .build();
    }

    @JsonIgnore
    private Date getTransactionDate() {
        return isPending() ? getPendingDate() : bookingDate;
    }

    private boolean isPending() {
        return STATUS_PENDING.equals(status) || !STATUS_BOOKED.equals(status);
    }

    @JsonIgnore
    private Date getPendingDate() {
        return Optional.ofNullable(expectingBookingDate).orElse(bookingDate);
    }
}
