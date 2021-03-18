package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.DisplayCategoryIndex;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Tag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.utils.CreditCardMasker;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ProductsEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ProductTypeEntity productType;

    private BalanceEntity originalBalance;

    private String identifier;

    private String iban;

    private String internalAccountNumber;

    private ProductIdEntity productId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CreditLimit creditLimit;

    private String creditCardNumber;

    // Commerzbank sometimes sends ProductId as null, which means we cannot map the account type
    public boolean hasValidProductId() {
        return productId != null;
    }

    public AccountTypes getType() {
        switch (productType.getDisplayCategoryIndex()) {
            case DisplayCategoryIndex.CHECKING:
                return AccountTypes.CHECKING;
            case DisplayCategoryIndex.SAVINGS_OR_INVESTMENT:
                return getSavingsOrInvestment();
            default:
                logger.warn(
                        "tag={} displayCategoryIndex: {}",
                        Tag.UNKNOWN_ACCOUNT_TYPE,
                        productType.getDisplayCategoryIndex());
                return AccountTypes.OTHER;
        }
    }

    private AccountTypes getSavingsOrInvestment() {
        if (StringUtils.containsIgnoreCase(
                productType.getProductName(), CommerzbankConstants.ACCOUNTS.SAVINGS_ACCOUNT)) {
            return AccountTypes.SAVINGS;
        }
        return AccountTypes.OTHER;
    }

    public ExactCurrencyAmount getTinkBalance() {
        return ExactCurrencyAmount.of(originalBalance.getValue(), originalBalance.getCurrency());
    }

    public ExactCurrencyAmount getTinkCredit() {
        return creditLimit.toTinkAmount();
    }

    public TransactionalAccount toTransactionalAccount() {
        AccountTypes accountType = getType();

        TransactionalAccount.Builder builder =
                TransactionalAccount.builder(accountType, iban, getTinkBalance())
                        .setName(productType.getProductName())
                        .setAccountNumber(internalAccountNumber)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                        .putInTemporaryStorage(Headers.IDENTIFIER, productId.getIdentifier())
                        .putInTemporaryStorage(Headers.PRODUCT_TYPE, productId.getProductType())
                        .putInTemporaryStorage(
                                Headers.PRODUCT_BRANCH, productType.getProductBranch());

        if (canMakePayment(accountType)) {
            builder.addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        }

        return builder.build();
    }

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(
                        se.tink.libraries.strings.StringUtils.hashAsStringMD5(identifier),
                        getTinkBalance(),
                        getTinkCredit())
                .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .setName(productType.getProductName())
                .setAccountNumber(CreditCardMasker.maskCardNumber(creditCardNumber))
                .putInTemporaryStorage(Headers.CREDIT_CARD_IDENTIFIER, productId.getIdentifier())
                .putInTemporaryStorage(Headers.CREDIT_CARD_PRODUCT_TYPE, productId.getProductType())
                .putInTemporaryStorage(Headers.PRODUCT_BRANCH, productType.getProductBranch())
                .build();
    }

    public boolean isCreditCard() {
        return productType.getDisplayCategoryIndex() == DisplayCategoryIndex.CREDIT;
    }

    private boolean canMakePayment(AccountTypes type) {
        List<AccountTypes> paymentAccountTypes =
                Arrays.asList(
                        AccountTypes.CHECKING, AccountTypes.SAVINGS, AccountTypes.CREDIT_CARD);
        return paymentAccountTypes.stream()
                .anyMatch(paymentAccountType -> paymentAccountType == type);
    }
}
