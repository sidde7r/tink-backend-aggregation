package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.loans;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SibsLoansFetcher implements AccountFetcher<LoanAccount> {

    private final SibsBaseApiClient apiClient;

    public SibsLoansFetcher(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();
        return accountsResponse.getAccountList().stream()
                .map(this::toTinkLoan)
                .collect(Collectors.toList());
    }

    private LoanAccount toTinkLoan(AccountEntity accountEntity) {
        ExactCurrencyAmount balanceAmount =
                apiClient.getAccountBalances(accountEntity.getId()).getBalances().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SibsConstants.ErrorMessages.NO_BALANCE))
                        .getInterimAvailable()
                        .getAmount()
                        .toTinkAmount();

        return accountEntity.toTinkLoan(balanceAmount);
    }
}
