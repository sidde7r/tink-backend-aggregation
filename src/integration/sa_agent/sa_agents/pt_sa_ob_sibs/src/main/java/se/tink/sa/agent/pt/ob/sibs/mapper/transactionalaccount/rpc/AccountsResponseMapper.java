package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.framework.mapper.ToDomainMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;

@Component
public class AccountsResponseMapper
        implements ToDomainMapper<FetchAccountsResponse, AccountsResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Override
    public FetchAccountsResponse mapToTransferModel(
            AccountsResponse source, MappingContext mappingContext) {
        FetchAccountsResponse.Builder destBuilder = FetchAccountsResponse.newBuilder();
        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon =
                requestToResponseCommonMapper.mapToTransferModel(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);

        return destBuilder.build();
    }
}
