package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private Date bookingDate;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private Date valueDate;
    private String debtorName;
    private String creditorName;

    public Transaction toPendingTransaction() {
        return toTinkTransaction(true);
    }

    public Transaction toBookedTransaction() {
        return toTinkTransaction(false);
    }

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDate(Optional.ofNullable(valueDate).orElse(bookingDate))
                .setPending(pending)
                .setAmount(transactionAmount.toAmount())
                .setDescription(createDescription())
                .build();
    }

    private String createDescription() {
        if (Objects.nonNull(this.remittanceInformationUnstructured)) {
            final String unstructured = this.remittanceInformationUnstructured;
            final String[] words = unstructured.split("\\s+");
            return String.join(" ", words);
        } else if (Objects.nonNull(this.debtorName)) {
            return this.debtorName;
        } else if (Objects.nonNull(this.creditorName)) {
            return this.creditorName;
        }
        throw new IllegalStateException("Couldn't find description");
    }
}
