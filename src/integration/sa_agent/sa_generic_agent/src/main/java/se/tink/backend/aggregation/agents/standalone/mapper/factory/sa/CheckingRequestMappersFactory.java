package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa.FetchAccountsRequestMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;

public class CheckingRequestMappersFactory {

    private final CommonExternalParametersProvider commonExternalParametersProvider;

    private CheckingRequestMappersFactory(
            CommonExternalParametersProvider commonExternalParametersProvider) {
        this.commonExternalParametersProvider = commonExternalParametersProvider;
    }

    public static CheckingRequestMappersFactory newInstance(
            CommonExternalParametersProvider commonExternalParametersProvider) {
        return new CheckingRequestMappersFactory(commonExternalParametersProvider);
    }

    public FetchAccountsRequestMapper fetchAccountsRequestMapper() {
        FetchAccountsRequestMapper mapper = new FetchAccountsRequestMapper();
        mapper.setConfiguredRequestParametersProvider(commonExternalParametersProvider);
        return mapper;
    }
}
