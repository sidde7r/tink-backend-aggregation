package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public abstract class CreditCardTransactionEntity {
    public abstract AggregationTransaction toTinkTransaction();
}
