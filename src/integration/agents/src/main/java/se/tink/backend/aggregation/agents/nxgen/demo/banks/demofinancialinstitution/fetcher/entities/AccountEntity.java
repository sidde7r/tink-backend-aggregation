package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities;

import static io.vavr.Predicates.anyOf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private long id;
    private String accountName;
    private String username;
    private AccountTypeEntity accountType;
    private List<AccountFlagEntity> accountFlags;
    private String accountNumber;
    private double balance;
    private String currency;

    public long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AccountTypeEntity getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountTypeEntity accountType) {
        this.accountType = accountType;
    }

    public List<AccountFlagEntity> getAccountFlags() {
        return accountFlags;
    }

    public void setAccountFlags(List<AccountFlagEntity> accountFlags) {
        this.accountFlags = accountFlags;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonIgnore
    private boolean hasAccountFlag(AccountFlagEntity accountFlag) {
        return getAccountFlags().stream().anyMatch(accountFlag::equals);
    }

    @JsonIgnore
    public boolean isPsd2Account() {
        return hasAccountFlag(AccountFlagEntity.PAYMENTS);
    }

    @JsonIgnore
    private Predicate<AccountTypes> isTinkTransactionalAccount() {
        return anyOf(
                AccountTypes.CHECKING::equals,
                AccountTypes.SAVINGS::equals,
                AccountTypes.OTHER::equals);
    }

    @JsonIgnore
    public Option<TransactionalAccount> maybeToTinkTransationalAccount() {
        return accountType
                .maybeToTinkAccountTypes()
                .filter(isTinkTransactionalAccount())
                .map(TransactionalAccountType::from)
                .map(this::toTinkTransactionalAccount);
    }

    @JsonIgnore
    private TransactionalAccount toTinkTransactionalAccount(
            TransactionalAccountType transactionalAccountType) {
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                // TODO: What should the identifier be? Clearing etc?
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .withBalance(BalanceModule.of(new Amount(currency, balance)))
                .addHolderName(username)
                .setApiIdentifier(accountNumber)
                .setBankIdentifier(accountNumber)
                .build();
    }
}
