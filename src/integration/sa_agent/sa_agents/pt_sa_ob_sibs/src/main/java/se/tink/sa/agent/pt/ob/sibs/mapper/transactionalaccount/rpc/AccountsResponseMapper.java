package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.account.TransactionalAccount;

@Component
public class AccountsResponseMapper implements Mapper<FetchAccountsResponse, AccountsResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Autowired private TransactionalAccountResponseMapper transactionalAccountResponseMapper;

    @Override
    public FetchAccountsResponse map(AccountsResponse source, MappingContext mappingContext) {
        FetchAccountsResponse.Builder destBuilder = FetchAccountsResponse.newBuilder();
        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon = requestToResponseCommonMapper.map(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);
        destBuilder.addAllAccount(mapTransactionsList(source.getAccountList(), mappingContext));

        return destBuilder.build();
    }

    private List<TransactionalAccount> mapTransactionsList(
            List<AccountEntity> accountList, MappingContext mappingContext) {
        List<TransactionalAccount> transactionalAccountList =
                Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                        .map(
                                account ->
                                        transactionalAccountResponseMapper.map(
                                                account, mappingContext))
                        .collect(Collectors.toList());

        return transactionalAccountList;
    }
}
