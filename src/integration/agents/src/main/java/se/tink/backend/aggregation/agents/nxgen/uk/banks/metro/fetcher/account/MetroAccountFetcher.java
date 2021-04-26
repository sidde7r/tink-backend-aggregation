package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account;

import com.google.inject.Inject;
import io.vavr.control.Either;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.AccountType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
public class MetroAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AccountClient accountClient;

    private final AccountType accountType;

    private final AccountMapper accountMapper;

    @Inject
    public MetroAccountFetcher(
            AccountClient accountClient, AccountType accountType, AccountMapper accountMapper) {
        this.accountClient = accountClient;
        this.accountType = accountType;
        this.accountMapper = accountMapper;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Either<AgentBankApiError, AccountsResponse> response = accountClient.accounts();
        if (response.isRight()) {
            AccountsResponse accountsEntity = response.get();
            return accountsEntity.getEntities(accountType).stream()
                    .flatMap(
                            entity ->
                                    accountMapper.map(entity.getAccounts(), entity.getEntityName())
                                            .stream())
                    .collect(Collectors.toList());
        } else {
            AgentBankApiError apiError = response.getLeft();
            log.error(
                    "An error has been occurred during fetching accounts. {}",
                    apiError.getDetails());
            return Collections.emptyList();
        }
    }
}
