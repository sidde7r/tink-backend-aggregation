package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class CrossKeyAccount {
    private String bban;
    private String bbanFormatted;
    private String accountId;
    private String accountNickname;
    private String currency;
    private double availableAmount;
    private double balance;
    private String bic;
    private String accountGroup;
    private String usageType;
    private String accountNumber;
    private double interestRate;
    private String accountTypeName;
    private int accountType;
    private String accountOwnerName;

    public Optional<TransactionalAccount> toTransactionalAccount(
            CrossKeyConfiguration agentConfiguration) {
        return agentConfiguration.parseTransactionalAccount(this);
    }

    public boolean isTransactionalAccount() {
        AccountTypes type = translateAccountType();
        return type == AccountTypes.CHECKING
                || type == AccountTypes.SAVINGS
                || type == AccountTypes.OTHER;
    }

    public AccountTypes translateAccountType() {
        if (CrossKeyConstants.Fetcher.Account.SAVING.equalsIgnoreCase(usageType)) {
            return AccountTypes.SAVINGS;
        }

        switch (Strings.nullToEmpty(accountGroup).toLowerCase()) {
            case CrossKeyConstants.Fetcher.Account.LOAN:
                return AccountTypes.LOAN;
            case CrossKeyConstants.Fetcher.Account.CHECK:
                return AccountTypes.CHECKING;
            case CrossKeyConstants.Fetcher.Account.INVESTMENT:
                return AccountTypes.INVESTMENT;
            default:
                return AccountTypes.OTHER;
        }
    }

    public boolean isInvestmentAccount() {
        AccountTypes type = translateAccountType();
        return type == AccountTypes.INVESTMENT;
    }

    public InvestmentAccount toInvestmentAccount(
            CrossKeyConfiguration agentConfiguration, Portfolio portfolio) {
        return agentConfiguration.parseInvestmentAccount(this, portfolio);
    }

    public Portfolio.Type getPortfolioType() {
        return CrossKeyConstants.PORTFOLIO_TYPES
                .translate(accountType)
                .orElse(Portfolio.Type.OTHER);
    }

    public boolean isLoan() {
        return CrossKeyConstants.Fetcher.Account.LOAN.equalsIgnoreCase(accountGroup);
    }

    public LoanDetails.Type getLoanType() {
        return CrossKeyConstants.LOAN_TYPES.translate(accountType).orElse(LoanDetails.Type.OTHER);
    }

    public LoanAccount toLoanAccount(
            CrossKeyConfiguration agentConfiguration, LoanDetailsEntity loanDetailsEntity) {
        return agentConfiguration.parseLoanAccount(this, loanDetailsEntity);
    }

    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public String getBbanFormatted() {
        return bbanFormatted;
    }

    public void setBbanFormatted(String bbanFormatted) {
        this.bbanFormatted = bbanFormatted;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNickname() {
        return accountNickname;
    }

    public void setAccountNickname(String accountNickname) {
        this.accountNickname = accountNickname;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(String accountGroup) {
        this.accountGroup = accountGroup;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getAccountType() {
        return accountType;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public Double getInterestRate() {
        return interestRate;
    }
}
