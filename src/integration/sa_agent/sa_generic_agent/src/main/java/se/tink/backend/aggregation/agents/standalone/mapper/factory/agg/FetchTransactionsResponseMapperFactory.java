package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.CommonMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.FetchTransactionsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.TransactionMapper;

public class FetchTransactionsResponseMapperFactory {

    private final FetchAccountsResponseMapperFactory fetchAccountsResponseMapperFactory;
    private final CommonMappersFactory commonMappersFactory;

    private FetchTransactionsResponseMapperFactory(
            FetchAccountsResponseMapperFactory fetchAccountsResponseMapperFactory,
            CommonMappersFactory commonMappersFactory) {
        this.fetchAccountsResponseMapperFactory = fetchAccountsResponseMapperFactory;
        this.commonMappersFactory = commonMappersFactory;
    }

    public static FetchTransactionsResponseMapperFactory newInstance(
            FetchAccountsResponseMapperFactory fetchAccountsResponseMapperFactory,
            CommonMappersFactory commonMappersFactory) {
        return new FetchTransactionsResponseMapperFactory(
                fetchAccountsResponseMapperFactory, commonMappersFactory);
    }

    public FetchTransactionsResponseMapper fetchTransactionsResponseMapper() {
        FetchTransactionsResponseMapper mapper = new FetchTransactionsResponseMapper();
        mapper.setTransactionAccountMapper(
                fetchAccountsResponseMapperFactory.transactionaAccountMapper());
        mapper.setTransactionMapper(transactionMapper());
        return mapper;
    }

    private TransactionMapper transactionMapper() {
        TransactionMapper mapper = new TransactionMapper();
        mapper.setGoogleDateMapper(commonMappersFactory.googleDateMapper());
        return mapper;
    }
}
