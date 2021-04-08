package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class JyskeBankAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final JyskeBankApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.fetchAccounts();

        return accountResponse.stream()
                .map(AccountsEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
