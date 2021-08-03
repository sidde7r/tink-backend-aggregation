package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalanceAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
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

    public String getIban() {
        return iban;
    }

    public String getBankId() {
        return Optional.ofNullable(bankId).orElse("");
    }

    public String getBban() {
        return bban;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProduct() {
        return product;
    }

    public AccountLinksEntity getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    private String getAccountName() {
        return (name != null) ? name : product;
    }

    // UniqueIdentifier for SE is bban. Don't change it.
    // EE hasn't bban, so UniqueIdentifier is set as iban
    private String getUniqueIdentifier() {
        return (bban != null) ? bban : iban;
    }

    private Collection<AccountIdentifier> getIdentifiers() {
        List<AccountIdentifier> identifiers = new ArrayList<>();

        // iban is presented for SE, EE
        // TODO: check LV and LT
        if (iban != null && !iban.isEmpty()) {
            identifiers.add(new IbanIdentifier(iban));
        }
        if (bban != null && !bban.isEmpty()) {
            // bban is not presented for EE
            // TODO: check LV and LT. SE has both iban and bban
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

    private static final Logger logger = LoggerFactory.getLogger(AccountEntity.class);

    public Optional<TransactionalAccount> toTinkAccount(List<BalancesItem> balances) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SwedbankConstants.ACCOUNT_TYPE_MAPPER, product)
                .withBalance(BalanceModule.of(getAvailableBalance(balances)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getUniqueIdentifier())
                                .withAccountName(getAccountName())
                                .addIdentifiers(getIdentifiers())
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(List<BalancesItem> balances) {
        return balances.stream()
                .map(BalancesItem::getBalanceAmount)
                .map(BalanceAmount::getAmount)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not fetch balance"));
    }
}
