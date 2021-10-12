package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class PendingTransactionBaseEntity extends TransactionDetailsBaseEntity {

    @Override
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(true)
                .setAmount(getPendingTransactionAmount())
                .setDate(Optional.ofNullable(bookingDate).orElse(valueDate))
                .setDescription(getTransactionDescription())
                .build();
    }

    private ExactCurrencyAmount getPendingTransactionAmount() {
        if (StringUtils.isNotBlank(creditorName)) {
            return transactionAmount.toAmount().abs().negate();
        }
        return transactionAmount.toAmount();
    }
}
