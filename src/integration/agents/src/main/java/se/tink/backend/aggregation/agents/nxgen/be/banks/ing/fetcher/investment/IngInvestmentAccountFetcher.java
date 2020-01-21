package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class IngInvestmentAccountFetcher
        implements AccountFetcher<InvestmentAccount>, TransactionPagePaginator<InvestmentAccount> {
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;

    public IngInvestmentAccountFetcher(IngApiClient apiClient, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.ingHelper = ingHelper;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return this.ingHelper
                .retrieveLoginResponse()
                .map(this::fetchAccountsFromLoginResponse)
                .orElseGet(Collections::emptyList);
    }

    private List<InvestmentAccount> fetchAccountsFromLoginResponse(
            LoginResponseEntity loginResponse) {
        return this.apiClient
                .fetchAccounts(loginResponse)
                .map(AccountsResponse::getAccounts)
                .map(AccountListEntity::stream)
                .orElseGet(Stream::empty)
                .filter(AccountEntity::isInvestmentType)
                .map(account -> account.toTinkInvestmentAccount())
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(InvestmentAccount account, int page) {
        String transactionsUrl = ingHelper.getUrl(IngConstants.RequestNames.GET_TRANSACTIONS);

        return apiClient.getTransactions(
                transactionsUrl,
                account.getApiIdentifier(),
                getStartIndex(page),
                getEndIndex(page));
    }

    private int getStartIndex(int page) {
        return (page * IngConstants.Fetcher.MAX_TRANSACTIONS_IN_BATCH) + 1;
    }

    private int getEndIndex(int page) {
        return (page + 1) * IngConstants.Fetcher.MAX_TRANSACTIONS_IN_BATCH;
    }
}
