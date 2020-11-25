package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.creditcard;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.CreditCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OpBankCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, URL> {

    private final OpBankApiClient apiClient;

    public OpBankCreditCardAccountFetcher(OpBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getCreditCards().stream()
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            CreditCardAccount account, URL nextUrl) {
        URL url =
                Optional.ofNullable(nextUrl)
                        .orElse(
                                Urls.GET_CREDIT_CARD_TRANSACTIONS.parameter(
                                        IdTags.CARD_ID,
                                        account.getFromTemporaryStorage(StorageKeys.CARD_ID)));

        return apiClient.getCreditCardTransactions(url);
    }
}
