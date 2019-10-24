package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Card;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.util.CurrencyMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
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
                .filter(obj -> Card.TYPE_CREDIT.equals(obj.get(Card.PRODUCT_CARD_TYPE)))
                .map(this::toTinkAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkAccount(Map<String, String> obj) {
        String accountCurrencyCode =
                currencyMapper.get(Integer.parseInt(obj.get(Card.CURRENCY))).getCurrencyCode();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(obj.get(Card.MASKED_NUMBER))
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                obj.get(Card.AUTHORIZED_BALANCE),
                                                accountCurrencyCode))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                obj.get(Card.AVAILABLE), accountCurrencyCode))
                                .withCardAlias(obj.get(Card.ALIAS))
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(obj.get(Card.ACCOUNT_NUMBER))
                                .withAccountNumber(obj.get(Card.ACCOUNT_NUMBER))
                                .withAccountName(obj.get(Card.PRODUCT_NAME))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER,
                                                obj.get(Card.MASKED_NUMBER)))
                                .setProductName(obj.get(Card.PRODUCT_NAME))
                                .build())
                .build();
    }
}
