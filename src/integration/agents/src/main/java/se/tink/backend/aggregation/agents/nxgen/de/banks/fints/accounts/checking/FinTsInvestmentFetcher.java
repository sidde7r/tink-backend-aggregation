package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class FinTsInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final FinTsApiClient apiClient;

    public FinTsInvestmentFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<SEPAAccount> accounts = apiClient.getSepaAccounts();

        return accounts.stream()
                .filter(sepaAccount -> sepaAccount.getExtensions() != null)
                .filter(
                        sepaAccount ->
                                sepaAccount
                                        .getExtensions()
                                        .contains(FinTsConstants.Segments.HKWPD.name()))
                .map(apiClient::getInvestment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
