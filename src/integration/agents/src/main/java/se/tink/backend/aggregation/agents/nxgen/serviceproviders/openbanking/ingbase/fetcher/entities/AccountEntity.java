package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
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
        String transactionsUrl = links.getTransactionsUrl();

        if (transactionsUrl == null) {
            // No transactions link, fallback to checking masked pan
            return Strings.nullToEmpty(maskedPan).isEmpty();
        }

        return !hasCardAccountTransactionsLink(transactionsUrl);
    }

    @JsonIgnore
    public boolean isCardAccount() {
        String transactionsUrl = links.getTransactionsUrl();

        if (transactionsUrl == null) {
            // No transactions link, fallback to checking masked pan
            return !Strings.nullToEmpty(maskedPan).isEmpty();
        }

        return hasCardAccountTransactionsLink(transactionsUrl);
    }

    /**
     * From documentation: In the response we will provide the product type being a card account.
     * Unfortunately they don't provide which product types are card accounts/current accounts. It's
     * something that they'll look into providing in the future.
     *
     * <p>Masked pan can be empty for card accounts as well so it's not reliable for determining
     * type. From documentation: The transactions endpoint for card accounts is different. For now
     * the transactions link is the most accurate way to determine if an account is a card account,
     * assuming that the link is present.
     */
    private boolean hasCardAccountTransactionsLink(String transactionsUrl) {
        return StringUtils.containsIgnoreCase(transactionsUrl, "card-accounts");
    }

    /**
     * To parse an account we need to have links to balances. Not checking this before parsing will
     * lead to NPE later. For now throws an IllegalStateException if the links aren't present. We
     * may want to handle this differently later.
     *
     * <p>Also logging product type as this is the first point where we process the account.
     * Hopefully it's not an unbound value and we can start using it to determine account type.
     */
    public boolean isParsableAccount() {
        log.info("ING product type: {}", product);

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
                                        .addIdentifier(new MaskedPanIdentifier(maskedPan))
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
