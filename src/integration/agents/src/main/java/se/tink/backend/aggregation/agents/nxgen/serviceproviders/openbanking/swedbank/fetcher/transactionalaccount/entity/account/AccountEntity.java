package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.MarketCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {
    private String bankId;
    private String bban;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String product;
    private String resourceId;
    private String name;
    private List<BalancesItem> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public String getBankId() {
        return Optional.ofNullable(bankId).orElse(StringUtils.EMPTY);
    }

    private String getAccountName() {
        return (name != null) ? name : product;
    }

    // UniqueIdentifier for SE is bban. Don't change it.
    // EE hasn't bban, so UniqueIdentifier is set as iban
    // this is market specific code to ensure we don't change unique identifier if EE, LT and LV
    // start sending bban
    private String getUniqueIdentifier(String market) {
        return market.equalsIgnoreCase(MarketCode.SE) ? bban : iban;
    }

    private Collection<AccountIdentifier> getIdentifiers(String market) {
        List<AccountIdentifier> identifiers = new ArrayList<>();

        String bic = getBic(market);

        // iban is presented for SE, EE, LV, LT
        identifiers.add(new IbanIdentifier(iban));

        if (!bic.equals(StringUtils.EMPTY)) {
            identifiers.add(new IbanIdentifier(bic, iban));
        }

        // SwedishIdentifier is only for SE
        if (market.equalsIgnoreCase(MarketCode.SE)) {
            identifiers.add(new SwedishIdentifier(bban));
        }
        return identifiers;
    }

    public String getResourceId() {
        return resourceId;
    }

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public Optional<TransactionalAccount> toTinkAccount(
            List<BalancesItem> balances, String market) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SwedbankConstants.ACCOUNT_TYPE_MAPPER, product)
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier(market))
                                .withAccountNumber(getUniqueIdentifier(market))
                                .withAccountName(getAccountName())
                                .addIdentifiers(getIdentifiers(market))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(resourceId)
                .build();
    }

    public Optional<TransactionalAccount> toBalticTinkAccount(
            List<BalancesItem> balances, String market, String holderName) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SwedbankConstants.ACCOUNT_TYPE_MAPPER, product)
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier(market))
                                .withAccountNumber(getUniqueIdentifier(market))
                                .withAccountName(getAccountName())
                                .addIdentifiers(getIdentifiers(market))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(resourceId)
                .addHolderName(holderName)
                .build();
    }

    private BalanceModule getBalanceModule(List<BalancesItem> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalancesItem> balances) {
        return balances.stream()
                .filter(BalancesItem::isBooked)
                .findFirst()
                .map(BalancesItem::toTinkAmount)
                .orElseThrow(() -> new IllegalStateException("Could not get booked balance."));
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalancesItem> balances) {
        return balances.stream()
                .filter(BalancesItem::isAvailable)
                .findFirst()
                .map(BalancesItem::toTinkAmount);
    }

    // Currently SE is not using bic
    private static String getBic(String market) {
        if (market.equalsIgnoreCase(MarketCode.LV)) {
            return BICProduction.LATVIA;
        } else if (market.equalsIgnoreCase(MarketCode.LT)) {
            return BICProduction.LITHUANIA;
        } else if (market.equalsIgnoreCase(MarketCode.EE)) {
            return BICProduction.ESTONIA;
        }
        return StringUtils.EMPTY;
    }
}
