package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.FetchTransactionsResponseMapper;

public class FetchTransactionsResponseMapperFactory {

    private TransactionEntityMapperFactory transactionEntityMapperFactory;
    private TransactionLinksEntityMapperFactory transactionLinksEntityMapperFactory;

    private FetchTransactionsResponseMapperFactory(
            TransactionEntityMapperFactory transactionEntityMapperFactory,
            TransactionLinksEntityMapperFactory transactionLinksEntityMapperFactory) {
        this.transactionEntityMapperFactory = transactionEntityMapperFactory;
        this.transactionLinksEntityMapperFactory = transactionLinksEntityMapperFactory;
    }

    public static FetchTransactionsResponseMapperFactory newInstance(
            TransactionEntityMapperFactory transactionEntityMapperFactory,
            TransactionLinksEntityMapperFactory transactionLinksEntityMapperFactory) {
        return new FetchTransactionsResponseMapperFactory(
                transactionEntityMapperFactory, transactionLinksEntityMapperFactory);
    }

    public FetchTransactionsResponseMapper fetchTransactionsResponseMapper() {
        FetchTransactionsResponseMapper mapper = new FetchTransactionsResponseMapper();
        mapper.setTransactionEntityMapper(
                transactionEntityMapperFactory.fetchTransactionEntityMapper());
        mapper.setTransactionLinksEntityMapper(
                transactionLinksEntityMapperFactory.fetchTransactionLinksEntityMapper());
        return mapper;
    }
}
