package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa.FetchTransactionsRequestMapper;

public class FetchTransactionsRequestMappersFactory {

    private FetchTransactionsRequestMappersFactory() {}

    public static FetchTransactionsRequestMappersFactory newInstance() {
        return new FetchTransactionsRequestMappersFactory();
    }

    public FetchTransactionsRequestMapper fetchTransactionsRequestMapper() {
        return new FetchTransactionsRequestMapper();
    }
}
