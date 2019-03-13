package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

@JsonObject
public class BnpParibasFortisTransactionalAccountFetcher implements
    AccountFetcher<TransactionalAccount>,
    TransactionKeyPaginator<TransactionalAccount, URL> {

    private final BnpParibasFortisApiClient apiClient;

    public BnpParibasFortisTransactionalAccountFetcher(BnpParibasFortisApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient
            .getAccounts()
            .getAccounts()
            .stream()
            .map(account -> account
                .toTinkModel(apiClient.getBalanceForAccount(account).getBalances()))
            .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(TransactionalAccount account,
        URL nextUrl) {
        try {
            GetTransactionsResponse response = apiClient.getTransactionsForAccount(account);
            response.setAccount(account);
            return response;
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
            }
            throw new IllegalStateException("Cannot fetch transactions", exception);
        }
    }
}
