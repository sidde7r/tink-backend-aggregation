package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class JyskeAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final JyskeApiClient apiClient;

    public JyskeAccountFetcher(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // TODO assume 0 is account type for checking
        // The app does not use the account type data from json directly
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(accountEntity -> accountEntity.getAccountType() == 0)
                .map(AccountEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }
}
