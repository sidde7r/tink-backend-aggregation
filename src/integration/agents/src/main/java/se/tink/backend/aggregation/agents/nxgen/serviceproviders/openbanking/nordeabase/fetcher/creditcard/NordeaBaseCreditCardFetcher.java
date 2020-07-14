package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaBaseCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {
    private final NordeaBaseApiClient apiClient;
    private final String currency;

    public NordeaBaseCreditCardFetcher(NordeaBaseApiClient apiClient, String currency) {
        this.apiClient = apiClient;
        this.currency = currency;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final List<CreditCardResponse> creditCards =
                apiClient.fetchCreditCards().getCards().stream()
                        .map(c -> apiClient.fetchCreditCardDetails(c.getId()))
                        .collect(Collectors.toList());

        return creditCards.stream()
                .map(CreditCardResponse::getCreditCard)
                .map(this::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        return apiClient.fetchCreditCardTransactions(account, key);
    }

    private CreditCardAccount toTinkCreditCard(CardsEntity cardsEntity) {
        return cardsEntity.toTinkCreditCard(currency);
    }
}
