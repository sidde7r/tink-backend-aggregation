package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardsEntity {
    private List<CardEntity> cards;
    private String id;
    private String maskedCreditCardNumber;
    private String maskedDebitCardNumber;
    private String productName;
    private String cardholderName;
    private String creditAccountNumber;
    private Double creditAvailableBalance;
    private Double creditBookedBalance;
    private Double creditLimit;
    private String mainCardholderName;

    public List<CardEntity> getCards() {
        return cards;
    }

    private String getCreditCardNumber() {
        return Strings.isNullOrEmpty(maskedCreditCardNumber)
                ? maskedDebitCardNumber
                : maskedCreditCardNumber;
    }

    // This specific format is to match the uniqueId for Nordea credit cards
    private String maskCreditCardNumber() {
        return "************"
                + maskedCreditCardNumber.substring(maskedCreditCardNumber.length() - 4);
    }

    public CreditCardAccount toTinkCreditCard(String currency) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(getCreditCardNumber())
                                .withBalance(
                                        ExactCurrencyAmount.of(creditAvailableBalance, currency))
                                .withAvailableCredit(ExactCurrencyAmount.of(creditLimit, currency))
                                .withCardAlias(productName)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskCreditCardNumber())
                                .withAccountNumber(maskCreditCardNumber())
                                .withAccountName(productName)
                                .addIdentifier(new MaskedPanIdentifier(maskCreditCardNumber()))
                                .setProductName(productName)
                                .build())
                .setApiIdentifier(id)
                // CardholderName is only returned in Norway
                .addHolderName(cardholderName)
                .build();
    }
}
