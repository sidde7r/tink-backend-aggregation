package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.sa;

import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;

public class FetchAccountsRequestMapper implements Mapper<FetchAccountsRequest, Void> {

    @Override
    public FetchAccountsRequest map(Void source, MappingContext context) {
        return null;
    }
}
