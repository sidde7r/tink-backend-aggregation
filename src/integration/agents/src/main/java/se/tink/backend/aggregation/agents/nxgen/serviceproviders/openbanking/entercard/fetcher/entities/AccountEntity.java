package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String customerID;
    private String accountNumber;
    private String accountType;
    private String timestamp;
    private String accountStatus;
    private BigDecimal otb;
    private BigDecimal creditLimit;
    private Number mtp;
    private Boolean eFaktura;
    private Boolean autogiro;
    private String productName;
    private String productDescription;
    private String country;
    private String currency;
    private String transactionPurposeList;
    private Number originalBalance;
    private BigDecimal balance;
    private Number terms;
    private Number interestBearingBalance;
    private Number nonInterestBearingBalance;
    private Number nominalInterestRate;
    private Number installmentCharges;
    private String installmentChargePeriod;
    private Number coApplicant;
    private Number reservedAmount;
    private Number balanceOnCardLoan;
    private Number bankID;
    private List<CardDetailsEntity> cardDetails;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return accountType.equalsIgnoreCase(AccountType.CREDIT_CARD);
    }

    @JsonIgnore
    public boolean isBrandId(String brandId) {
        return StringUtils.containsIgnoreCase(productName, brandId);
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(getCardNumber())
                                .withBalance(getAccountBalance())
                                .withAvailableCredit(getAvailableCredit())
                                .withCardAlias(getName())
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(getCardNumber())
                                .withAccountName(getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                accountNumber))
                                .setProductName(productName)
                                .build())
                .addHolderName(getName())
                .setApiIdentifier(accountNumber)
                .build();
    }

    private ExactCurrencyAmount getAccountBalance() {
        return ExactCurrencyAmount.of(creditLimit.subtract(otb).negate(), getAccountCurrency());
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.of(otb, getAccountCurrency());
    }

    private String getAccountCurrency() {
        return Optional.ofNullable(currency).orElse(EnterCardConstants.DEFAULT_CURRENCY);
    }

    @JsonIgnore
    private String getCardNumber() {

        return cardDetails.stream()
                .findFirst()
                .map(CardDetailsEntity::getMaskedCardNo)
                .orElse(StringUtils.EMPTY);
    }

    private String getName() {
        return cardDetails.stream()
                .findFirst()
                .map(CardDetailsEntity::getCardHolderName)
                .orElse(StringUtils.EMPTY);
    }
}
