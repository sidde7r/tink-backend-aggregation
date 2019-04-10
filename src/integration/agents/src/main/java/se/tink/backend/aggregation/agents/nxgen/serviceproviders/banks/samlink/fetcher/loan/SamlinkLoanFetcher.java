package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SamlinkLoanFetcher implements AccountFetcher<LoanAccount> {
    private final SamlinkApiClient apiClient;

    public SamlinkLoanFetcher(SamlinkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        LoansResponse loansResponse = apiClient.getLoans();

        if (Objects.nonNull(loansResponse.getLoans())) {
            return loansResponse.getLoans().stream()
                    .filter(le -> le.getDetailsLink().isPresent())
                    .map(
                            le -> {
                                String detailsLink =
                                        le.getDetailsLink().orElseThrow(IllegalStateException::new);
                                return apiClient.getLoanDetails(detailsLink).toAccount();
                            })
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
