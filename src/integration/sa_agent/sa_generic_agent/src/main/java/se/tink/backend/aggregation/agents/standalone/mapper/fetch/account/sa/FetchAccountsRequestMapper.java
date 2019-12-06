package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;

public class FetchAccountsRequestMapper implements Mapper<FetchAccountsRequest, Void> {

    private CommonExternalParametersProvider configuredRequestParametersProvider;

    public void setConfiguredRequestParametersProvider(
            CommonExternalParametersProvider configuredRequestParametersProvider) {
        this.configuredRequestParametersProvider = configuredRequestParametersProvider;
    }

    @Override
    public FetchAccountsRequest map(Void source, MappingContext mappingContext) {
        FetchAccountsRequest.Builder requestBuilder = FetchAccountsRequest.newBuilder();

        requestBuilder.putAllExternalParameters(
                configuredRequestParametersProvider.buildExternalParametersMap());
        return requestBuilder.build();
    }
}
