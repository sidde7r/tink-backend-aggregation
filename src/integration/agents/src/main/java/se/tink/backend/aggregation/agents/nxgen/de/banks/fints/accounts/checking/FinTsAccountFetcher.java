package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsAccountTypeConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FinTsAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FinTsApiClient apiClient;

    public FinTsAccountFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        apiClient.getAccounts().stream()
                .filter(
                        sepaAccount ->
                                !sepaAccount.getSupportedSegments().isEmpty()
                                        && sepaAccount
                                                .getSupportedSegments()
                                                .contains(FinTsConstants.Segments.HKSAL.name()))
                .forEach(apiClient::getBalance);

        return apiClient.getSepaAccounts().stream()
                // Filter non-transactional accounts
                .filter(
                        sepaAccount ->
                                AccountTypes.SAVINGS.equals(
                                                FinTsAccountTypeConverter.getAccountTypeFor(
                                                        sepaAccount.getAccountType()))
                                        || AccountTypes.CHECKING.equals(
                                                FinTsAccountTypeConverter.getAccountTypeFor(
                                                        sepaAccount.getAccountType())))
                .map(SEPAAccount::toTinkAccount)
                .collect(Collectors.toList());
    }
}
