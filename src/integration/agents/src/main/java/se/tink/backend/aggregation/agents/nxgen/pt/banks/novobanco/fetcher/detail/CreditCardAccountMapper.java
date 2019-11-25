package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.CardDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.CardListEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardAccountMapper {

    public static List<CreditCardAccount> mapToTinkAccounts(
            CardListEntity cardList, AccountDetailsEntity accountDetails) {
        List<CreditCardAccount> creditCardAccounts = new ArrayList<>();
        String currency = getCurrency(cardList);
        List<CardDetailsEntity> cardsDetails = cardList.getCardsDetails();
        cardsDetails.forEach(
                details -> {
                    CreditCardModule cardDetails = getCreditCardModule(details, currency);
                    CreditCardAccount account =
                            CreditCardAccount.nxBuilder()
                                    .withCardDetails(cardDetails)
                                    .withInferredAccountFlags()
                                    .withId(getId(details, accountDetails))
                                    .build();
                    creditCardAccounts.add(account);
                });
        return creditCardAccounts;
    }

    private static IdModule getId(
            CardDetailsEntity cardDetails, AccountDetailsEntity accountDetails) {
        return IdModule.builder()
                .withUniqueIdentifier(cardDetails.getCardNumber())
                .withAccountNumber(accountDetails.getId())
                .withAccountName(accountDetails.getDesc())
                .addIdentifier(
                        AccountIdentifierProvider.getAccountIdentifier(
                                accountDetails.getIban(), accountDetails.getId()))
                .build();
    }

    private static CreditCardModule getCreditCardModule(
            CardDetailsEntity details, String currency) {
        return CreditCardModule.builder()
                .withCardNumber(details.getCardNumber())
                .withBalance(ExactCurrencyAmount.of(getBalance(details), currency))
                .withAvailableCredit(ExactCurrencyAmount.of(getAvailableCredit(details), currency))
                .withCardAlias(getBrandName(details))
                .build();
    }

    private static String getCurrency(CardListEntity cardList) {
        return Optional.of(cardList)
                .map(CardListEntity::getCurrency)
                .orElseThrow(() -> new IllegalStateException("Currency information is missing"));
    }

    private static BigDecimal getBalance(CardDetailsEntity cardDetails) {
        return Optional.of(cardDetails)
                .map(CardDetailsEntity::getCreditLimit)
                .orElseThrow(() -> new IllegalStateException("Balance information is missing"));
    }

    private static String getBrandName(CardDetailsEntity cardDetails) {
        return Optional.of(cardDetails)
                .map(CardDetailsEntity::getBrand)
                .orElseThrow(() -> new IllegalStateException("Brand information is missing"));
    }

    private static BigDecimal getAvailableCredit(CardDetailsEntity cardDetails) {
        return Optional.of(cardDetails)
                .map(CardDetailsEntity::getAvailableBalance)
                .orElseThrow(
                        () -> new IllegalStateException("Available Credit information is missing"));
    }
}
