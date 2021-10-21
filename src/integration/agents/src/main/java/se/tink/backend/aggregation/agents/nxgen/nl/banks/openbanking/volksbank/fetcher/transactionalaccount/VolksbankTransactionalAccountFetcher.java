package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils.VolksbankUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class VolksbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final VolksbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        final String consentId = VolksbankUtils.getConsentIdFromStorage(persistentStorage);

        final OAuth2Token oauth2Token = VolksbankUtils.getOAuth2TokenFromStorage(persistentStorage);

        List<AccountsEntity> accounts =
                apiClient.fetchAccounts(consentId, oauth2Token).getAccounts();

        return accounts.stream()
                .map(account -> account.toTinkAccount(getBalances(account, consentId, oauth2Token)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<BalanceEntity> getBalances(
            AccountsEntity account, String consentId, OAuth2Token oauth2Token) {
        return apiClient.readBalance(account, consentId, oauth2Token).getBalances();
    }
}
