package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final VolksbankApiClient apiClient;
    private final ConsentFetcher consentFetcher;
    private final PersistentStorage persistentStorage;

    public VolksbankTransactionalAccountFetcher(
            final VolksbankApiClient apiClient,
            final ConsentFetcher consentFetcher,
            final PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.consentFetcher = consentFetcher;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String consentId = consentFetcher.fetchConsent();

        final OAuth2Token oauth2Token =
                persistentStorage
                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));

        List<AccountsEntity> accounts =
                apiClient.fetchAccounts(consentId, oauth2Token).getAccounts();

        return accounts.stream()
                .map(
                        account -> {
                            List<BalanceEntity> balances =
                                    apiClient
                                            .readBalance(account, consentId, oauth2Token)
                                            .getBalances();
                            return account.toTinkAccount(balances);
                        })
                .collect(Collectors.toList());
    }
}
