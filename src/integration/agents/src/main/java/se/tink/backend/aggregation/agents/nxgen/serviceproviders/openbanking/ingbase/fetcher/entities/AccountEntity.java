package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlinGroupBalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String maskedPan;
    private String name;
    private String currency;
    private String product;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getResourceId() {
        return resourceId;
    }

    @JsonIgnore
    public String getBalancesUrl() {
        return links.getBalancesUrl();
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return Strings.nullToEmpty(maskedPan).isEmpty();
    }

    @JsonIgnore
    public boolean isCardAccount() {
        return !Strings.nullToEmpty(maskedPan).isEmpty();
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(
            List<BalanceEntity> balances, boolean lowercaseAccountId) {
        if (!isTransactionalAccount()) {
            throw new IllegalStateException("Not a transactional account.");
        }
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier(lowercaseAccountId))
                                .withAccountNumber(iban)
                                .withAccountName(product)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(maskedPan)
                .putInTemporaryStorage(IngBaseConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .putInTemporaryStorage(
                        IngBaseConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl())
                .build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount(List<BalanceEntity> balances) {
        if (!isCardAccount()) {
            throw new IllegalStateException("Not a credit card account.");
        }
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(BerlinGroupBalanceMapper.getBookedBalance(balances))
                                .withAvailableCredit(
                                        BerlinGroupBalanceMapper.getAvailableBalance(balances)
                                                .orElse(ExactCurrencyAmount.zero(currency)))
                                .withCardAlias(maskedPan)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(resourceId)
                                .withAccountNumber(maskedPan)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, maskedPan))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .putInTemporaryStorage(IngBaseConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .putInTemporaryStorage(
                        IngBaseConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl())
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder()
                        .withBalance(BerlinGroupBalanceMapper.getBookedBalance(balances));
        BerlinGroupBalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BerlinGroupBalanceMapper.getCreditLimit(balances)
                .ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }

    @JsonIgnore
    public String getUniqueIdentifier(boolean lowercase) {
        if (lowercase) {
            return iban.toLowerCase(Locale.ROOT);
        }
        return iban.toUpperCase(Locale.ROOT);
    }
}
