package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.rpc.PensionAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@RequiredArgsConstructor
public class PensionAccountFetcher implements AccountFetcher<InvestmentAccount> {
    private final MinPensionApiClient minPensionApiClient;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> pensionAccounts = new ArrayList<>();
        final PensionAccountsResponse pensionAccountsResponse =
                minPensionApiClient.fetchPensionAccounts();
        final String ssn = minPensionApiClient.fetchSsn();

        final InvestmentAccount generalPension =
                pensionAccountsResponse.getGeneralPension().toTinkInvestmentAccount(ssn);

        pensionAccounts.add(generalPension);

        return pensionAccounts;
    }
}
