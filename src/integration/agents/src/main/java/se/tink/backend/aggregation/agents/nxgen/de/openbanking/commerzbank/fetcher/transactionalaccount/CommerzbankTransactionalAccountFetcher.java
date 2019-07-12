package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class CommerzbankTransactionalAccountFetcher
        extends Xs2aDevelopersTransactionalAccountFetcher {

    private final CommerzbankApiClient commerzbankApiClient;

    public CommerzbankTransactionalAccountFetcher(
            Xs2aDevelopersApiClient apiClient, CommerzbankApiClient commerzbankApiClient) {
        super(apiClient);
        this.commerzbankApiClient = commerzbankApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountsResponse getAccountsResponse = commerzbankApiClient.createAccount();

        List<TransactionalAccount> accounts = new ArrayList<>();
        for (AccountsEntity accountsEntity : getAccountsResponse.getAccounts()) {
            BalanceEntity balanceEntity =
                    commerzbankApiClient
                            .createBalance(accountsEntity.getLinks().getBalances().getHref())
                            .getBalances()
                            .get(0);
            accounts.add(accountsEntity.toTinkAccount(balanceEntity).get());
        }
        return accounts;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    commerzbankApiClient
                            .createTransactions(account, fromDate, toDate)
                            .toTinkTransactions());
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
