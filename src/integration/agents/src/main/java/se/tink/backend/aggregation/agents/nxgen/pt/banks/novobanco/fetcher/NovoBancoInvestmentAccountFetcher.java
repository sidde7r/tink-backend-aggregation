package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.InvestmentAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NovoBancoInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    private final NovoBancoApiClient apiClient;

    public NovoBancoInvestmentAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Collection<GetInvestmentsResponse> investmentResponses = apiClient.getInvestments();
        return investmentResponses.stream()
                .filter(GetInvestmentsResponse::isSuccessful)
                .map(InvestmentAccountMapper::mapToTinkAccount)
                .collect(Collectors.toList());
    }
}
