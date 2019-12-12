package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class FetchAccountsResponseMapper
        implements Mapper<
                List<TransactionalAccount>,
                se.tink.sa.services.fetch.account.FetchAccountsResponse> {

    private TransactionAccountMapper transactionaAccountMapper;

    public void setTransactionaAccountMapper(TransactionAccountMapper transactionaAccountMapper) {
        this.transactionaAccountMapper = transactionaAccountMapper;
    }

    @Override
    public List<TransactionalAccount> map(
            se.tink.sa.services.fetch.account.FetchAccountsResponse source,
            MappingContext mappingContext) {

        return mapAccountList(source.getAccountList(), mappingContext);
    }

    private List<TransactionalAccount> mapAccountList(
            final List<se.tink.sa.services.fetch.account.TransactionalAccount> accountList,
            MappingContext mappingContext) {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                .map(acc -> transactionaAccountMapper.map(acc, mappingContext))
                .collect(Collectors.toList());
    }
}
