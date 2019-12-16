package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa.FetchTransactionsRequestMapper;

public class FetchTransactionsRequestMappersFactory {

    private RequestCommonMapper requestCommonMapper;

    private FetchTransactionsRequestMappersFactory(RequestCommonMapper requestCommonMapper) {
        this.requestCommonMapper = requestCommonMapper;
    }

    public static FetchTransactionsRequestMappersFactory newInstance(
            CommonMappersFactory commonMappersFactory) {
        return new FetchTransactionsRequestMappersFactory(
                commonMappersFactory.requestCommonMapper());
    }

    public FetchTransactionsRequestMapper fetchTransactionsRequestMapper() {
        FetchTransactionsRequestMapper fetchTransactionsRequestMapper =
                new FetchTransactionsRequestMapper();
        fetchTransactionsRequestMapper.setRequestCommonMapper(requestCommonMapper);
        return fetchTransactionsRequestMapper;
    }
}
