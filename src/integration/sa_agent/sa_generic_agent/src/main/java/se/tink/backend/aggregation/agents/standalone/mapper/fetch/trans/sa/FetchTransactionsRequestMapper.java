package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;

public class FetchTransactionsRequestMapper
        implements Mapper<FetchTransactionsRequest, TransactionalAccount> {

    private RequestCommonMapper requestCommonMapper;

    public void setRequestCommonMapper(RequestCommonMapper requestCommonMapper) {
        this.requestCommonMapper = requestCommonMapper;
    }

    @Override
    public FetchTransactionsRequest map(
            TransactionalAccount source, MappingContext mappingContext) {
        FetchTransactionsRequest.Builder destBuilder =
                se.tink.sa.services.fetch.trans.FetchTransactionsRequest.newBuilder();
        destBuilder.setAccountId(source.getApiIdentifier());

        String nextKey = mappingContext.get(MappingContextKeys.NEXT_TR_PAGE_LINK);
        if (nextKey != null) {
            destBuilder.putExternalParameters(MappingContextKeys.NEXT_TR_PAGE_LINK, nextKey);
        }

        destBuilder.setRequestCommon(requestCommonMapper.map(null, mappingContext));

        return destBuilder.build();
    }
}
