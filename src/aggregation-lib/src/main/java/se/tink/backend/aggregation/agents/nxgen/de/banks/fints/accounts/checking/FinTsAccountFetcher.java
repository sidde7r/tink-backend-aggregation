package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class FinTsAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FinTsApiClient apiClient;

    public FinTsAccountFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        apiClient.getAccounts()
                .stream()
                .filter(sepaAccount -> sepaAccount.getExtensions().contains(FinTsConstants.Segments.HKSAL))
                .forEach(apiClient::getBalance);

        return apiClient.getSepaAccounts().stream()
                // Filter non-transactional accounts
                .filter(sepaAccount -> sepaAccount.getAccountType() < 20)
                .map(SEPAAccount::toTinkAccount).collect(Collectors.toList());
    }
}
