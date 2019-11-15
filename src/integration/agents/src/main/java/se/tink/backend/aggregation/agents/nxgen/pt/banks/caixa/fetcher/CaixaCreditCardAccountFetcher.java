package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.time.YearMonth;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardAccountTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CreditBalancesEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
                .map(this::toTinkAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkAccount(CardEntity card) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(buildCardDetails(card))
                .withInferredAccountFlags()
                .withId(buildCardAccountId(card))
                .addHolderName(card.getPrintedName())
                .setApiIdentifier(card.getCardAccountId())
                .build();
    }

    private CreditCardModule buildCardDetails(CardEntity card) {
        CardAccountTransactionsEntity balances =
                apiClient
                        .fetchCardAccountTransactions(card.getCardAccountId(), YearMonth.now())
                        .getCardAccountTransactions();
        CreditBalancesEntity limits =
                apiClient.fetchCardAccountDetails(card.getCardAccountId()).getAccountBalances();

        return CreditCardModule.builder()
                .withCardNumber(card.getMaskedCardNumber())
                .withBalance(
                        ExactCurrencyAmount.of(
                                balances.getCreditTotal(), card.getCardAccountCurrency()))
                .withAvailableCredit(
                        ExactCurrencyAmount.of(limits.getAvailableCredit(), limits.getCurrency()))
                .withCardAlias(
                        ObjectUtils.firstNonNull(
                                card.getCardAlias(), card.getCardAccountDescription()))
                .build();
    }

    private IdModule buildCardAccountId(CardEntity card) {
        return IdModule.builder()
                .withUniqueIdentifier(card.getCardKey())
                .withAccountNumber(card.getCardAccountNumber())
                .withAccountName(card.getCardAccountDescription())
                .addIdentifier(
                        AccountIdentifier.create(
                                Type.PAYMENT_CARD_NUMBER, card.getMaskedCardNumber()))
                .build();
    }
}
