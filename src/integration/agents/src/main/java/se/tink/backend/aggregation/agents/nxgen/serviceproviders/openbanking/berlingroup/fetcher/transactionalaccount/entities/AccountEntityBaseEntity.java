package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntityBaseEntity implements BerlinGroupAccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public AccountEntityBaseEntity() {}

    public AccountEntityBaseEntity(
            String resourceId,
            String iban,
            String currency,
            String name,
            String cashAccountType,
            AccountLinksEntity links,
            List<BalanceBaseEntity> balances) {
        this.resourceId = resourceId;
        this.iban = iban;
        this.currency = currency;
        this.name = name;
        this.cashAccountType = cashAccountType;
        this.balances = balances;
        this.links = links;
    }

    @Override
    public Optional<TransactionalAccount> toTinkAccount() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .flatMap(this::toTransactionalAccount);
    }

    private Optional<TransactionalAccount> toTransactionalAccount(TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(cashAccountType)
                                .addIdentifier(getIdentifier())
                                .build())
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .addHolderName(name)
                .build();
    }

    @Override
    public ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    @Override
    public boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    @Override
    public ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.zero(currency);
    }

    public String getIban() {
        return iban;
    }

    @Override
    public String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactionLink).orElse("");
    }

    @Override
    public String getUniqueIdentifier() {
        return iban;
    }

    @Override
    public AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    @Override
    public String getAccountNumber() {
        return iban;
    }

    @Override
    public String getBalancesLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getBalanceLink).orElse("");
    }

    public List<BalanceBaseEntity> getBalances() {
        return balances;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }

    public AccountLinksEntity getLinks() {
        return links;
    }

    public void setBalances(final List<BalanceBaseEntity> balances) {
        this.balances = balances;
    }
}
