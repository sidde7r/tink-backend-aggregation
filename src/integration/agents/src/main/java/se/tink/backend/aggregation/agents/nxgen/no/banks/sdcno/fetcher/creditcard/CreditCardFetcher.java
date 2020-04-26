package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class CreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final SdcNoApiClient bankClient;

    private static final String CURRENCY = "NOK";
    private static final String STATUS = "ACTIVE";
    private static final String TYPE = "CREDIT";

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return bankClient.fetchCreditCards().stream()
                .map(this::mapToCardEntityObject)
                .filter(this::filterCreditCards)
                .map(this::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    private CardEntity mapToCardEntityObject(LinkedHashMap<String, String> jsonObject) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(jsonObject, CardEntity.class);
    }

    private CreditCardAccount toTinkCreditCard(CardEntity card) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(card.getCardNumber())
                                .withBalance(ExactCurrencyAmount.of(card.getAmount(), CURRENCY))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(card.getAvailableAmount(), CURRENCY))
                                .withCardAlias(card.getCardName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(card.getAccountId())
                                .withAccountNumber(card.getCardNumber())
                                .withAccountName(card.getCardName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, card.getCardNumber()))
                                .setProductName(card.getCardName())
                                .build())
                .setApiIdentifier(card.getAccountId())
                .build();
    }

    private boolean filterCreditCards(CardEntity card) {
        return STATUS.equals(card.getStatus()) && TYPE.equals(card.getType());
    }
}
