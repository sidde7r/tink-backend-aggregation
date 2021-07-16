package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactions.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionDetailsBaseEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TriodosPendingTransactionEntity extends TransactionDetailsBaseEntity {

    @JsonProperty("transactionAmount")
    protected TriodosBalanceAmountEntity triodosBalanceAmountEntity;

    @Override
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setPending(true)
                        .setAmount(triodosBalanceAmountEntity.toAmount(isDebit()))
                        .setDate(Optional.ofNullable(bookingDate).orElse(valueDate))
                        .setDescription(getTransactionDescription());

        if (shouldSetPayload()) {
            builder.setPayload(
                    TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL, getCounterPartyName());
        }

        return (Transaction) builder.build();
    }

    private boolean shouldSetPayload() {
        return getCounterPartyName() != null;
    }

    private String getCounterPartyName() {
        return Stream.of(creditorName, debtorName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean isDebit() {
        return debtorAccount != null;
    }
}
