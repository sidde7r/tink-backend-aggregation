package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
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

    /**
     * To parse an account we need to have links to balances. Not checking this before parsing will
     * lead to NPE later. For now throws an IllegalStateException if the links aren't present. We
     * may want to handle this differently later.
     */
    public boolean isParsableAccount() {
        if (links.getBalancesUrl() == null) {
            throw new IllegalStateException("Balances link not present, can't parse account.");
        }

        return true;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(
            List<BalanceEntity> balances, MarketConfiguration marketConfiguration) {
        if (!isTransactionalAccount()) {
            throw new IllegalStateException("Not a transactional account.");
        }

        TransactionalBuildStep buildStep =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(getBalanceModule(balances))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                getUniqueIdentifier(
                                                        marketConfiguration
                                                                .shouldReturnLowercaseAccountId()))
                                        .withAccountNumber(iban)
                                        .withAccountName(product)
                                        .addIdentifier(new IbanIdentifier(iban))
                                        .build())
                        .addParties(marketConfiguration.convertHolderNamesToParties(name))
                        .setApiIdentifier(resourceId)
                        .setBankIdentifier(maskedPan)
                        .putInTemporaryStorage(IngBaseConstants.StorageKeys.ACCOUNT_ID, resourceId);

        if (links.getTransactionsUrl() != null) {
            buildStep.putInTemporaryStorage(
                    IngBaseConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl());
        }

        return buildStep.build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount(List<BalanceEntity> balances) {
        if (!isCardAccount()) {
            throw new IllegalStateException("Not a credit card account.");
        }

        CreditCardBuildStep buildStep =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(maskedPan)
                                        .withBalance(BalanceMapper.getBookedBalance(balances))
                                        .withAvailableCredit(
                                                BalanceMapper.getAvailableBalance(balances)
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
                                                        AccountIdentifierType.MASKED_PAN,
                                                        maskedPan))
                                        .build())
                        .addHolderName(name)
                        .setApiIdentifier(resourceId)
                        .setBankIdentifier(resourceId)
                        .putInTemporaryStorage(IngBaseConstants.StorageKeys.ACCOUNT_ID, resourceId);

        if (links.getTransactionsUrl() != null) {
            buildStep.putInTemporaryStorage(
                    IngBaseConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl());
        }

        return buildStep.build();
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
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
