package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.TransactionLinksEntityMapper;

public class TransactionLinksEntityMapperFactory {

    private TransactionLinksEntityMapperFactory() {}

    public static TransactionLinksEntityMapperFactory newInstance() {
        return new TransactionLinksEntityMapperFactory();
    }

    public TransactionLinksEntityMapper fetchTransactionLinksEntityMapper() {
        TransactionLinksEntityMapper transactionLinksEntityMapper =
                new TransactionLinksEntityMapper();
        return transactionLinksEntityMapper;
    }
}
