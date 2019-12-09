package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa.FetchAccountsRequestMapper;

public class CheckingRequestMappersFactory {

    private final CommonMappersFactory commonMappersFactory;

    private CheckingRequestMappersFactory(CommonMappersFactory commonMappersFactory) {
        this.commonMappersFactory = commonMappersFactory;
    }

    public static CheckingRequestMappersFactory newInstance(
            CommonMappersFactory commonMappersFactory) {
        return new CheckingRequestMappersFactory(commonMappersFactory);
    }

    public FetchAccountsRequestMapper fetchAccountsRequestMapper() {
        FetchAccountsRequestMapper mapper = new FetchAccountsRequestMapper();
        mapper.setRequestCommonMapper(commonMappersFactory.requestCommonMapper());
        return mapper;
    }
}
