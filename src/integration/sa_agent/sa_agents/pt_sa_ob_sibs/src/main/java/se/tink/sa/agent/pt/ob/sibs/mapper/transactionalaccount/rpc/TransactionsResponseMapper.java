package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;

@Component
public class TransactionsResponseMapper
        implements Mapper<FetchTransactionsResponse, TransactionsResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Override
    public FetchTransactionsResponse mapToTransferModel(
            TransactionsResponse source, MappingContext mappingContext) {
        FetchTransactionsResponse.Builder destBuilder = FetchTransactionsResponse.newBuilder();
        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon =
                requestToResponseCommonMapper.mapToTransferModel(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);

        return destBuilder.build();
    }
}
