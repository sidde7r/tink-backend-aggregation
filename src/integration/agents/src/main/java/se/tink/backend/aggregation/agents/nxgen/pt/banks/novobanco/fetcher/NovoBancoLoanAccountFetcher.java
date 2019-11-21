package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.LoanAccountMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class NovoBancoLoanAccountFetcher implements AccountFetcher<LoanAccount> {
    private final NovoBancoApiClient apiClient;

    public NovoBancoLoanAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<NovoBancoApiClient.LoanAggregatedData> loansResponseData = apiClient.getLoanAccounts();
        return loansResponseData.stream()
                .map(LoanAccountMapper::mapToTinkAccount)
                .collect(Collectors.toList());
    }
}
