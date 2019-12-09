package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa;

import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;

public class FetchTransactionsRequestMapper
        implements Mapper<
                FetchTransactionsRequest,
                se.tink.sa.services.fetch.trans.FetchTransactionsRequest> {
    @Override
    public FetchTransactionsRequest map(
            FetchTransactionsRequest source, MappingContext mappingContext) {
        return se.tink.sa.services.fetch.trans.FetchTransactionsRequest.newBuilder().build();
    }
}
