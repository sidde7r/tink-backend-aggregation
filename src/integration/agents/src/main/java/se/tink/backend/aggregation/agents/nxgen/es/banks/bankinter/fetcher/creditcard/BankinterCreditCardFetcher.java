package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BankinterCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, PaginationKey> {
    private BankinterApiClient apiClient;

    public BankinterCreditCardFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return this.apiClient.fetchGlobalPosition().getCreditCardLinks().stream()
                .map(this::fetchCreditCardAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<CreditCardAccount> fetchCreditCardAccount(String accountLink) {
        final CreditCardResponse cardResponse = apiClient.fetchCreditCard(accountLink);
        if (!cardResponse.isCreditCard()) {
            return Optional.empty();
        }

        return Optional.of(cardResponse.toCreditCardAccount(accountLink));
    }

    public TransactionsResponse fetchTransactionsPage(
            CreditCardAccount account, PaginationKey key) {
        if (Objects.isNull(key)) {
            // first page, get view state from account
            final CreditCardResponse cardResponse =
                    apiClient.fetchCreditCard(account.getApiIdentifier());
            key = cardResponse.getFirstPaginationKey();
        }

        final String submitKey = key.getSource().split(":")[0] + FormValues.SUBMIT;

        return apiClient.fetchJsfUpdate(
                Urls.CREDIT_CARD,
                submitKey,
                key.getSource(),
                key.getViewState(),
                TransactionsResponse.class,
                JsfPart.CARD_TRANSACTIONS,
                JsfPart.CARD_TRANSACTIONS_NAVIGATION);
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationKey> getTransactionsFor(
            CreditCardAccount account, PaginationKey key) {
        final TransactionsResponse response = fetchTransactionsPage(account, key);

        TransactionKeyPaginatorResponseImpl<PaginationKey> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();
        paginatorResponse.setTransactions(response.toTinkTransactions());
        paginatorResponse.setNext(response.getNextKey());
        return paginatorResponse;
    }
}
