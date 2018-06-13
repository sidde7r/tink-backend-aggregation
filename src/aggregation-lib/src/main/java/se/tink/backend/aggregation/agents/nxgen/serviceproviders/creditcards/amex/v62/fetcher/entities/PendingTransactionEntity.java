package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public abstract class PendingTransactionEntity extends TransactionEntity {

    public UpcomingTransaction toUpcomingTransaction(AmericanExpressV62Configuration configuration) {
        return UpcomingTransaction.builder()
                .setAmount(configuration.toAmount((getAmount().getRawValue())))
                .setDescription(getDescription().stream().collect(Collectors.joining("\n")))
                .setDate(getDate())
                .build();
    }
}
