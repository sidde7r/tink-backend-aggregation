package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.CardCategory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardEntity {
    // Unique id of the card (required)
    private String cardId;
    // Card category (required)
    private String cardCategory;
    // Status of the card (required)
    private String cardStatus;
    // Cardholder's name
    private String cardholderName;
    // Cardholder's type, with following type meanings:
    // - principal = Principal cardholder
    // - parallel = Parallel cardholder
    // - joint = Parallel cardholder with joint credit liability
    private String cardholderType;
    // Principal cardholder's name. Same as cardholder name if cardholder type is 'principal'
    private String principalCardholderName;
    // Unique code that unambiguously maps to a commercial product name, card image, etc. (required)
    private String productCode;
    // Information of card loyalty group for Stockmann MasterCards
    private String cardLoyaltyGroup;
    // Specifies country the card belongs to. Represented as 2 letter ISO 3166-1 alpha-2 country
    // code (required)
    private String countryCode;
    // Currency of the card or the account the card is linked to. Represented as ISO 4217 currency
    // code (required)
    private String currency;
    // Card's nickname, assigned by the user for the card
    private String nickname;
    // Card PAN ID
    private String panId;
    // Bank account number for ATM withdrawals in IBAN format
    private String atmAccountNumber;
    // Card's credit feature information. Not available for debit cards
    private CardCreditDetails credit;

    @JsonIgnore
    private boolean isCreditCard() {
        return credit != null
                && (cardCategory.equalsIgnoreCase(CardCategory.CREDIT)
                        || cardCategory.equalsIgnoreCase(CardCategory.COMBINED));
    }

    @JsonIgnore
    public Optional<CreditCardAccount> toTinkCreditCardAccount() {
        if (!isCreditCard()) {
            return Optional.empty();
        }

        final String maskedCreditCardNumber = credit.getMaskedCreditCardNumber();
        final String cardAlias = MoreObjects.firstNonNull(nickname, maskedCreditCardNumber);

        final CreditCardBuildStep builder =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(maskedCreditCardNumber)
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        MoreObjects.firstNonNull(
                                                                credit.getCreditAvailableBalance(),
                                                                BigDecimal.ZERO),
                                                        currency))
                                        .withAvailableCredit(
                                                ExactCurrencyAmount.of(
                                                        MoreObjects.firstNonNull(
                                                                credit.getCreditLimit(),
                                                                BigDecimal.ZERO),
                                                        currency))
                                        .withCardAlias(cardAlias)
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(cardId)
                                        .withAccountNumber(maskedCreditCardNumber)
                                        .withAccountName(cardAlias)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                        maskedCreditCardNumber))
                                        .build())
                        .setApiIdentifier(cardId);

        if (!Strings.isNullOrEmpty(cardholderName)) {
            builder.addHolderName(cardholderName);
        }

        return Optional.of(builder.build());
    }
}
