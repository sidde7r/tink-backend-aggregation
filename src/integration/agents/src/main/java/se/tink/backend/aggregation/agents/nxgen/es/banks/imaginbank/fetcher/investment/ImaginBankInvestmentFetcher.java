package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.investment;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class ImaginBankInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private ImaginBankApiClient apiClient;

    public ImaginBankInvestmentFetcher(ImaginBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(AccountEntity::isInvestmentAccount)
                .map(
                        accountEntity ->
                                accountEntity.toTinkInvestmentAccount(
                                        apiClient.fetchPartiesForAccount(accountEntity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
