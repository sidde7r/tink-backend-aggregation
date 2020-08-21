package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class LoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private final IspApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();
        List<AccountEntity> accounts =
                accountsResponse.getPayload().getAccountViews().stream()
                        .flatMap(a -> a.getAccounts().stream())
                        .collect(Collectors.toList());
        return accounts.stream()
                .filter(AccountEntity::isLoanAccount)
                .map(AccountEntity::toLoanAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
