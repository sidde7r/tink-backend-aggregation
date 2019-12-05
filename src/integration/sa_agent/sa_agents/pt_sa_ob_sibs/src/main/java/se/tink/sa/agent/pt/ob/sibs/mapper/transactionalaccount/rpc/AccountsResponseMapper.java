package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.framework.mapper.ToDomainMapper;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;

@Component
public class AccountsResponseMapper
        implements ToDomainMapper<FetchAccountsResponse, AccountsResponse> {

    @Override
    public FetchAccountsResponse mapToTransferModel(
            AccountsResponse source, MappingContext mappingContext) {
        return null;
    }
}
