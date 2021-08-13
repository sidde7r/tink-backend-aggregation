package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.DisplayCategoryIndex;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Tag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.WithFlagPolicyStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.utils.CreditCardMasker;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
public class ProductsEntity {
    private ProductTypeEntity productType;

    private BalanceEntity originalBalance;
    private CreditLineDetailsEntity creditLineDetails;

    private String identifier;

    private String bic;
    private String iban;

    private String internalAccountNumber;

    private ProductIdEntity productId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BalanceEntity creditLimit;

    private String creditCardNumber;

    private ReferenceAccountEntity referenceAccount;

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
                log.warn(
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

    public ExactCurrencyAmount toTinkBalance(BalanceEntity balanceEntity) {
        return ExactCurrencyAmount.of(balanceEntity.getValue(), balanceEntity.getCurrency());
    }

    public Optional<TransactionalAccount> toTransactionalAccount(ItemsEntity itemEntity) {
        AccountTypes accountType = getType();

        WithFlagPolicyStep<WithBalanceStep<TransactionalBuildStep>, TransactionalAccountTypeMapper>
                partialBuilder =
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.from(accountType).orElse(null));

        WithBalanceStep<TransactionalBuildStep> partialBuilderWithFlags;
        if (canMakePayment(accountType)) {
            partialBuilderWithFlags = partialBuilder.withPaymentAccountFlag();
        } else {
            partialBuilderWithFlags = partialBuilder.withoutFlags();
        }

        return partialBuilderWithFlags
                .withBalance(buildBalanceModuleForTransactionalAccount())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(internalAccountNumber)
                                .withAccountName(productType.getProductName())
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .addIdentifier(new BbanIdentifier(iban.substring(4)))
                                .build())
                .addParties(
                        new Party(
                                itemEntity.getCustomerName(),
                                itemEntity.isPrimary() ? Party.Role.HOLDER : Party.Role.UNKNOWN))
                .setApiIdentifier(productId.getIdentifier())
                .putInTemporaryStorage(Headers.IDENTIFIER, productId.getIdentifier())
                .putInTemporaryStorage(Headers.PRODUCT_TYPE, productId.getProductType())
                .putInTemporaryStorage(Headers.PRODUCT_BRANCH, productType.getProductBranch())
                .build();
    }

    private BalanceModule buildBalanceModuleForTransactionalAccount() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(toTinkBalance(originalBalance));

        Optional.ofNullable(creditLineDetails)
                .map(CreditLineDetailsEntity::getCreditLine)
                .map(x -> ExactCurrencyAmount.of(x.getValue(), x.getCurrency()))
                .ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }

    public CreditCardAccount toCreditCardAccount(ItemsEntity itemEntity) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(CreditCardMasker.maskCardNumber(creditCardNumber))
                                .withBalance(toTinkBalance(originalBalance))
                                .withAvailableCredit(toTinkBalance(creditLimit))
                                .withCardAlias(productType.getProductName())
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(
                                        se.tink.libraries.strings.StringUtils.hashAsStringMD5(
                                                identifier))
                                .withAccountNumber(referenceAccount.getIban())
                                .withAccountName(referenceAccount.getIban())
                                .addIdentifier(new IbanIdentifier(referenceAccount.getIban()))
                                .addIdentifier(
                                        new MaskedPanIdentifier(
                                                CreditCardMasker.maskCardNumber(creditCardNumber)))
                                .build())
                .addParties(
                        new Party(
                                itemEntity.getCustomerName(),
                                itemEntity.isPrimary() ? Party.Role.HOLDER : Party.Role.UNKNOWN))
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
