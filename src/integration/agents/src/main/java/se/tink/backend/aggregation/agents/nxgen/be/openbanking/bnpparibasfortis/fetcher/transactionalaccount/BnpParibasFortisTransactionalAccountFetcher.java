package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Account;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account.Links;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http.BnpParibasFortisApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@JsonObject
public class BnpParibasFortisTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final BnpParibasFortisApiClient apiClient;

    public BnpParibasFortisTransactionalAccountFetcher(BnpParibasFortisApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .filter(this::isCheckingAccount)
                .map(acc -> acc.toTinkModel(apiClient.getBalanceForAccount(acc).getBalances()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isCheckingAccount(Account account) {
        return BnpParibasFortisConstants.ACCOUNT_TYPE_MAPPER
                .translate(account.getCashAccountType())
                .map(AccountTypes.CHECKING::equals)
                .orElse(false);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        final String baseUrl = Urls.BASE_PATH + Urls.PSD2_BASE_PATH;

        final String transactionsUrl =
                Optional.ofNullable(nextUrl)
                        .map(url -> baseUrl + nextUrl)
                        .orElseGet(() -> getUrlFromStorage(account, baseUrl));

        try {
            return apiClient.getTransactionsForAccount(transactionsUrl);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
            }
            throw new IllegalStateException("Cannot fetch transactions", exception);
        }
    }

    private String getUrlFromStorage(TransactionalAccount account, String baseUrl) {
        return account.getFromTemporaryStorage(StorageKeys.ACCOUNT_LINKS, Links.class)
                .map(links -> baseUrl + links.getTransactions().getHref())
                .orElseThrow(IllegalStateException::new);
    }
}
