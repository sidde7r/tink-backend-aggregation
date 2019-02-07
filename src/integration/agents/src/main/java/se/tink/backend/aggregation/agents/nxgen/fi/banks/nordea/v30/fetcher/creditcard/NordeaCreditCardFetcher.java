package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionIndexPaginator<CreditCardAccount> {

    private final NordeaFiApiClient apiClient;

    public NordeaCreditCardFetcher(
            NordeaFiApiClient client) {
        this.apiClient = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {

        return apiClient
                .fetchCards()
                .toTinkCards();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int numberOfTransactions, int startIndex) {

        return apiClient
                .fetchTransactions(startIndex, numberOfTransactions, account.getBankIdentifier(),
                        NordeaFiConstants.Products.CARD, FetchCardTransactionResponse.class)
                .toPaginatorResponse(account);
    }
}
