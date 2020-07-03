package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.loans;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.SibsBaseTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsLoansFetcher extends SibsBaseTransactionFetcher
        implements AccountFetcher<LoanAccount>, TransactionKeyPaginator<LoanAccount, String> {

    public SibsLoansFetcher(
            SibsBaseApiClient apiClient,
            CredentialsRequest credentialsRequest,
            SibsUserState userState) {
        super(apiClient, credentialsRequest, userState);
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

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            LoanAccount account, String key) {
        if (StringUtils.isNotEmpty(key)) {
            key = key.replace(StringUtils.SPACE, ENCODED_SPACE);
        }
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(
                        () ->
                                apiClient.getAccountTransactions(
                                        account, getTransactionsFetchBeginDate(account)));
    }
}
