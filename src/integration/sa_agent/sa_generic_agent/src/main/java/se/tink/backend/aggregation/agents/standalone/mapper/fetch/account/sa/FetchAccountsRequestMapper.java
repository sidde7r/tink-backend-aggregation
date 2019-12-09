package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;

public class FetchAccountsRequestMapper implements Mapper<FetchAccountsRequest, Void> {

    private RequestCommonMapper requestCommonMapper;

    public void setRequestCommonMapper(RequestCommonMapper requestCommonMapper) {
        this.requestCommonMapper = requestCommonMapper;
    }

    @Override
    public FetchAccountsRequest map(Void source, MappingContext mappingContext) {
        FetchAccountsRequest.Builder requestBuilder = FetchAccountsRequest.newBuilder();
        requestBuilder.setRequestCommon(requestCommonMapper.map(source, mappingContext));
        return requestBuilder.build();
    }
}
