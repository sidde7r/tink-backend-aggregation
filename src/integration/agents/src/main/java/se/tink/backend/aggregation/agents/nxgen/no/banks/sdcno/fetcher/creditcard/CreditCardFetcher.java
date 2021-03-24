package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class CreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final SdcNoApiClient bankClient;

    private static final String CURRENCY = "NOK";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String CREDIT_CARD_TYPE = "CREDIT";

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return bankClient.fetchCreditCards().stream()
                .filter(this::filterCreditCards)
                .map(this::toTinkCreditCard)
                .collect(Collectors.toList());
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
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                card.getCardNumber()))
                                .setProductName(card.getCardName())
                                .build())
                .setApiIdentifier(card.getAccountId())
                .build();
    }

    private boolean filterCreditCards(CardEntity card) {
        return ACTIVE_STATUS.equals(card.getStatus()) && CREDIT_CARD_TYPE.equals(card.getType());
    }
}
