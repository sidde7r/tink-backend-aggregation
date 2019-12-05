package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class FetchAccountsResponseMapper
        implements Mapper<
                FetchAccountsResponse, se.tink.sa.services.fetch.account.FetchAccountsResponse> {

    private TransactionaAccountMapper transactionaAccountMapper;

    public void setTransactionaAccountMapper(TransactionaAccountMapper transactionaAccountMapper) {
        this.transactionaAccountMapper = transactionaAccountMapper;
    }

    @Override
    public FetchAccountsResponse map(
            se.tink.sa.services.fetch.account.FetchAccountsResponse source,
            MappingContext mappingContext) {
        FetchAccountsResponse fetchAccountsResponse =
                new FetchAccountsResponse(mapAccountList(source.getAccountList(), mappingContext));
        return fetchAccountsResponse;
    }

    private List<Account> mapAccountList(
            final List<se.tink.sa.services.fetch.account.TransactionalAccount> accountList,
            MappingContext mappingContext) {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                .map(acc -> transactionaAccountMapper.map(acc, mappingContext))
                .collect(Collectors.toList());
    }
}
