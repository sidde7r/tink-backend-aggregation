package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardsEntity {
    private List<CardEntity> cards;
    private String id;

    @JsonProperty("masked_credit_card_number")
    private String maskedCreditCardNumber;

    @JsonProperty("masked_debit_card_number")
    private String maskedDebitCardNumber;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("cardholder_name")
    private String cardholderName;

    @JsonProperty("credit_account_number")
    private String creditAccountNumber;

    @JsonProperty("credit_available_balance")
    private Double creditAvailableBalance;

    @JsonProperty("credit_booked_balance")
    private Double creditBookedBalance;

    @JsonProperty("credit_limit")
    private Double creditLimit;

    @JsonProperty("main_cardholder_name")
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
                                .withAvailableCredit(ExactCurrencyAmount.inSEK(creditLimit))
                                .withCardAlias(productName)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskCreditCardNumber())
                                .withAccountNumber(maskCreditCardNumber())
                                .withAccountName(productName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                maskCreditCardNumber()))
                                .setProductName(productName)
                                .build())
                .setApiIdentifier(id)
                .build();
    }
}
