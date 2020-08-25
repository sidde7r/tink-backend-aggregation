package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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
        // Bank responds with 403 HTTP error if user has not have given consent to fetch credit
        // cards. This is a quick temp fix to return empty list if that happens while we implement
        // another solution where we fetch the given scopes that the agent can fetch.
        try {
            final List<CreditCardResponse> creditCards =
                    apiClient.fetchCreditCards().getCards().stream()
                            .map(c -> apiClient.fetchCreditCardDetails(c.getId()))
                            .collect(Collectors.toList());

            return creditCards.stream()
                    .map(CreditCardResponse::getCreditCard)
                    .map(this::toTinkCreditCard)
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                return Collections.emptyList();
            }
            throw hre;
        }
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
