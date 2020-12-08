package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalanceAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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
        return bankId;
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

    public String getResourceId() {
        return resourceId;
    }

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public Optional<TransactionalAccount> toTinkAccount(List<BalancesItem> balances) {

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance(balances)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .putInTemporaryStorage(SwedbankConstants.StorageKeys.ACCOUNT_ID, iban)
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
