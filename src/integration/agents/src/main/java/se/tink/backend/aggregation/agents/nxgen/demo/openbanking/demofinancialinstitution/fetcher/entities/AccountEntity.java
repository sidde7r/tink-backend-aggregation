package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.entities;

import static io.vavr.Predicates.anyOf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
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
                .withBalance(BalanceModule.of(new Amount(currency, balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                // TODO: What should the identifier be? Clearing etc?
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .addHolderName(username)
                .setApiIdentifier(accountNumber)
                .setBankIdentifier(accountNumber)
                .build();
    }

    @JsonIgnore
    private Predicate<AccountTypes> isTinkCreditCardAccount() {
        return anyOf(AccountTypes.CREDIT_CARD::equals);
    }

    @JsonIgnore
    public Option<CreditCardAccount> maybeToTinkCreditCardAccount() {
        return accountType
                .maybeToTinkAccountTypes()
                .filter(isTinkCreditCardAccount())
                .map(t -> toTinkCreditCardAccount());
    }

    @JsonIgnore
    private CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.builder(accountNumber)
                .setName(accountName)
                .setBalance(new Amount(currency, balance))
                .setHolderName(new HolderName(username))
                .setBankIdentifier(accountNumber)
                .setAccountNumber(accountNumber)
                .build();
    }

    @JsonIgnore
    private Predicate<AccountTypes> isTinkLoanAccount() {
        return anyOf(AccountTypes.LOAN::equals, AccountTypes.MORTGAGE::equals);
    }

    @JsonIgnore
    public Option<LoanAccount> maybeToTinkLoanAccount() {
        return accountType
                .maybeToTinkAccountTypes()
                .filter(isTinkLoanAccount())
                .map(t -> toTinkLoanAccount());
    }

    @JsonIgnore
    private LoanAccount toTinkLoanAccount() {
        final Double interestRate = 0.05;

        return LoanAccount.builder(accountNumber, Amount.inSEK(balance))
                .setName(accountName)
                .setAccountNumber(accountNumber)
                .setBankIdentifier(accountNumber)
                .setInterestRate(interestRate)
                .build();
    }

    @JsonIgnore
    private Predicate<AccountTypes> isTinkInvestmentAccount() {
        return anyOf(AccountTypes.INVESTMENT::equals, AccountTypes.PENSION::equals);
    }

    @JsonIgnore
    public Option<InvestmentAccount> maybeToTinkInvestmentAccount() {
        return accountType
                .maybeToTinkAccountTypes()
                .filter(isTinkInvestmentAccount())
                .map(t -> toTinkInvestmentAccount());
    }

    @JsonIgnore
    private InvestmentAccount toTinkInvestmentAccount() {
        final List<Portfolio> portfolios = new ArrayList<>();

        return InvestmentAccount.builder(accountNumber)
                .setName(accountName)
                .setAccountNumber(accountNumber)
                .setHolderName(new HolderName(username))
                .setCashBalance(new Amount(currency, balance))
                .setPortfolios(portfolios)
                .build();
    }
}
