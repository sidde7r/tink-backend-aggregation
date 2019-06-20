package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentHoldingsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class SkandiaBankenInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenInvestmentFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return apiClient.fetchInvestments().getSecuritiesAccounts().stream()
                .map(this::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    private InvestmentAccount toTinkInvestmentAccount(SecuritiesAccountsEntity accountsEntity) {
        final String investmentAccountNumber = accountsEntity.getEncryptedNumber();
        final FetchInvestmentHoldingsResponse holdingsResponse =
                apiClient.fetchHoldings(investmentAccountNumber);
        return apiClient
                .fetchInvestmentAccountDetails(investmentAccountNumber)
                .toTinkInvestmentAccount(accountsEntity, holdingsResponse);
    }
}
