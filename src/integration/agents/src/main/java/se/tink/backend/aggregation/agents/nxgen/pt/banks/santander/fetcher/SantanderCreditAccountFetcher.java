package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Card;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderCreditAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final SantanderApiClient apiClient;
    private final CurrencyMapper currencyMapper;

    public SantanderCreditAccountFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<Map<String, String>> businessData = apiClient.fetchCards().getBusinessData();
        return businessData.stream()
                .filter(card -> Card.TYPE_CREDIT.equals(card.get(Card.PRODUCT_CARD_TYPE)))
                .map(this::toTinkAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkAccount(Map<String, String> card) {
        String accountCurrencyCode =
                currencyMapper.get(Integer.parseInt(card.get(Card.CURRENCY))).getCurrencyCode();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(card.get(Card.MASKED_NUMBER))
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                card.get(Card.AUTHORIZED_BALANCE),
                                                accountCurrencyCode))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                card.get(Card.AVAILABLE), accountCurrencyCode))
                                .withCardAlias(card.get(Card.ALIAS))
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(card.get(Card.ACCOUNT_NUMBER))
                                .withAccountNumber(card.get(Card.ACCOUNT_NUMBER))
                                .withAccountName(card.get(Card.PRODUCT_NAME))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                card.get(Card.MASKED_NUMBER)))
                                .setProductName(card.get(Card.PRODUCT_NAME))
                                .build())
                .setApiIdentifier(card.get(Card.FULL_NUMBER))
                .putInTemporaryStorage(STORAGE.CURRENCY_CODE, accountCurrencyCode)
                .build();
    }
}
