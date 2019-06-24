package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksWithHrefEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BerlinGroupAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity implements BerlinGroupAccountEntity {
    protected String resourceId;
    protected String iban;
    protected String currency;
    protected String name;
    protected String cashAccountType;

    protected List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    protected AccountLinksWithHrefEntity links;

    @Override
    public boolean isCheckingOrSavingsType() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER.translate(cashAccountType).isPresent();
    }

    @Override
    public TransactionalAccount toTinkAccount() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .filter(this::isCheckingType)
                .map(accountType -> toCheckingAccount())
                .orElse(toSavingsAccount());
    }

    @Override
    public boolean isCheckingType(final AccountTypes accountType) {
        return accountType == AccountTypes.CHECKING;
    }

    @Override
    public TransactionalAccount toCheckingAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(getIdentifier())
                                .build())
                .withBalance(BalanceModule.of(getBalance()))
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .addHolderName(name)
                .build();
    }

    @Override
    public TransactionalAccount toSavingsAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(getIdentifier())
                                .build())
                .withBalance(BalanceModule.of(getBalance()))
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .addHolderName(name)
                .build();
    }

    @Override
    public Amount getBalance() {
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
    public Amount getDefaultAmount() {
        return new Amount(currency, 0);
    }

    public String getIban() {
        return iban;
    }

    @Override
    public String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    @Override
    public String getUniqueIdentifier() {
        return resourceId;
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
    public AccountIdentifier getAccountIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban);
    }

    @Override
    public String getBalancesLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getBalanceLink)
                .orElse("");
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

    public AccountLinksWithHrefEntity getLinks() {
        return links;
    }

    public void setBalances(final List<BalanceBaseEntity> balances) {
        this.balances = balances;
    }
}
