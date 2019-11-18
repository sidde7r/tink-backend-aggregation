package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.time.YearMonth;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardAccountTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CreditBalancesEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CaixaCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private static final String CREDIT_CARD_ACCOUNT = "CREDIT";
    private final CaixaApiClient apiClient;

    public CaixaCreditCardAccountFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCardAccounts().getCardAccounts().stream()
                .filter(acc -> CREDIT_CARD_ACCOUNT.equals(acc.getType()))
                .flatMap(cardAccountEntity -> cardAccountEntity.getCards().stream())
                .map(this::mapCard)
                .collect(Collectors.toList());
    }

    private CreditCardAccount mapCard(CardEntity card) {
        CardAccountTransactionsEntity balances =
                apiClient
                        .fetchCardAccountTransactions(card.getCardAccountId(), YearMonth.now())
                        .getCardAccountTransactions();

        CreditBalancesEntity limits =
                apiClient.fetchCardAccountDetails(card.getCardAccountId()).getAccountBalances();

        return card.toTinkAccount(balances, limits);
    }
}
