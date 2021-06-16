package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaBaseCreditCardFetcher<R extends TransactionKeyPaginatorResponse<String>>
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {
    private final NordeaBaseApiClient apiClient;
    private final Class<R> responseClass;
    private final String currency;

    public NordeaBaseCreditCardFetcher(
            NordeaBaseApiClient apiClient, String currency, Class<R> responseClass) {
        this.apiClient = apiClient;
        this.currency = currency;
        this.responseClass = responseClass;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final String[] scopes = apiClient.fetchUserAssets().getScopes();

        // only fetch credit cards if the user has given consent
        if (Arrays.stream(scopes).anyMatch(Scopes.CARDS_INFORMATION::equalsIgnoreCase)) {
            final List<CreditCardResponse> creditCards =
                    apiClient.fetchCreditCards().getCards().stream()
                            .map(c -> apiClient.fetchCreditCardDetails(c.getId()))
                            .collect(Collectors.toList());

            return creditCards.stream()
                    .map(CreditCardResponse::getCreditCard)
                    .map(this::toTinkCreditCard)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        return apiClient.fetchCreditCardTransactions(account, key, responseClass);
    }

    private CreditCardAccount toTinkCreditCard(CardsEntity cardsEntity) {
        return cardsEntity.toTinkCreditCard(currency);
    }
}
