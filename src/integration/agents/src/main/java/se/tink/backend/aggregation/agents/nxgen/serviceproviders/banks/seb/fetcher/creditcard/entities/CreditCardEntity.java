package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardEntity {

    @JsonIgnore private static final String SEB_MASTERCARD = "SEB:s MasterCard";

    @JsonIgnore private static final String SPECIAL_MASTERCARD_PATTERN = "%s (MasterCard)";
    @JsonIgnore private static final String BASIC_CARD_TYPE_CODE = "B";
    @JsonIgnore private static final String SPECIAL_CARD_TYPE_CODE = "S";
    @JsonIgnore private static final String EUROCARD_PRODUCT_NAME = "eurocard";
    @JsonIgnore private static final String SEB_CARD_PRODUCT_NAME = "seb";

    @JsonProperty("BILL_UNIT_HDL")
    private String handle;

    @JsonProperty("KORTNR")
    private String cardNumber;

    @JsonProperty("LIMIT_BELOPP")
    private int creditLimit;

    @JsonProperty("NAME_ON_CARD")
    private String holderName;

    @JsonProperty("SALDO_BELOPP")
    private double rawBalance;

    @JsonProperty("PRODUKT_NAMN")
    private String productName;

    @JsonProperty("KORT_TYP")
    private String cardTypeEncoded;

    @JsonProperty("KONTO_ID")
    private String accountId;

    public CreditCardAccount toTinkAccount() {
        ExactCurrencyAmount balance =
                new ExactCurrencyAmount(
                        BigDecimal.valueOf(rawBalance).negate(), SebConstants.DEFAULT_CURRENCY);
        ExactCurrencyAmount availableCredit =
                new ExactCurrencyAmount(new BigDecimal(creditLimit), SebConstants.DEFAULT_CURRENCY);

        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(cardNumber)
                        .withBalance(balance)
                        .withAvailableCredit(availableCredit)
                        .withCardAlias(productName)
                        .build();

        String uniqueId = cardNumber.replaceAll("\\*+", "").replaceAll("\\s+", "");

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(uniqueId)
                        .withAccountNumber(cardNumber)
                        .withAccountName(getCreditCardAccountName())
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.PAYMENT_CARD_NUMBER, cardNumber))
                        .build();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardModule)
                .withoutFlags()
                .withId(idModule)
                .addHolderName(holderName)
                .build();
    }

    private String getCreditCardAccountName() {

        if (isBasicMastercard(cardTypeEncoded)) {
            return SEB_MASTERCARD;
        }

        if (isSpecialCard(cardTypeEncoded) && productName != null) {

            String productNameLowerCase = productName.toLowerCase();

            if (isEuroCard(productNameLowerCase)) {
                return productName;
            }

            if (isOtherSebProduct(productNameLowerCase)) {
                return String.format(SPECIAL_MASTERCARD_PATTERN, productName);
            }
        }

        return productName;
    }

    @JsonIgnore
    private boolean isBasicMastercard(final String cardTypeEncoded) {
        return BASIC_CARD_TYPE_CODE.equals(cardTypeEncoded);
    }

    @JsonIgnore
    private boolean isSpecialCard(final String cardTypeEncoded) {
        return SPECIAL_CARD_TYPE_CODE.equals(cardTypeEncoded);
    }

    @JsonIgnore
    private boolean isEuroCard(final String productName) {
        return productName.contains(EUROCARD_PRODUCT_NAME);
    }

    @JsonIgnore
    private boolean isOtherSebProduct(final String productName) {
        return productName.contains(SEB_CARD_PRODUCT_NAME);
    }

    @JsonIgnore
    public String getHandle() {
        return this.handle;
    }
}
