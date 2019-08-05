package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;
    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    public boolean isCheckingType() {
        return DeutscheBankConstants.ACCOUNT_TYPE_MAPPER.translate(cashAccountType).isPresent();
    }

    public TransactionalAccount toTinkAccount() {
        return DeutscheBankConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .filter(this::isCheckingType)
                .map(accountType -> toCheckingAccount())
                .orElse(toSavingsAccount());
    }

    public boolean isCheckingType(final AccountTypes accountType) {
        return accountType == AccountTypes.CHECKING;
    }

    public TransactionalAccount toCheckingAccount() {
        return toTinkAccountWithType(TransactionalAccountType.CHECKING);
    }

    public TransactionalAccount toSavingsAccount() {
        return toTinkAccountWithType(TransactionalAccountType.SAVINGS);
    }

    private TransactionalAccountType getAccountType() {
        return isCheckingType()
                ? TransactionalAccountType.CHECKING
                : TransactionalAccountType.SAVINGS;
    }

    private TransactionalAccount toTinkAccountWithType(TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountType().toString())
                                .addIdentifier(getIdentifier())
                                .build())
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .build();
    }

    public ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Unable to fetch balance"));
    }

    public boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    public String getIban() {
        return iban;
    }

    public String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    public String getUniqueIdentifier() {
        return resourceId;
    }

    public AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    public String getAccountNumber() {
        return iban;
    }

    public AccountIdentifier getAccountIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban);
    }

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
