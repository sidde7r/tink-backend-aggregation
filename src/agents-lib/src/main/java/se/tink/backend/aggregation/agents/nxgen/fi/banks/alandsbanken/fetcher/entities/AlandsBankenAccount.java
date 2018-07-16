package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AlandsBankenAccount {
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

//    Unused fields, but possibly of interest.
//    private String accountTypeName;
//    private String accountSubGroup;
//    private double creditLimit;
//    private double trueInterestRate;
//    private double interestMargin;
//    private String capitalization;
//    private double minInterestRate;
//    private double maxInterestRate;
//    private String referenceInterestName;
//    private double referenceInterestValue;
//    private String accountOwnerName;
//    private String accountCoOwnerName;
//    private boolean moreOwnersThanTwo;
//    private boolean owner;
//    private boolean softLocked;
//    private boolean pledged;
//    private boolean payableAccount;
//    private boolean transferable;
//    private boolean defaultAccount;
//    private boolean showAccount;
//    private boolean reservations;
//    private boolean reservationAmount;
//    private boolean allowedAsDefaultAccount;
//    private boolean transferableToOwnAccount;
//    private Date dueDate;
//    private String grossInterestAmount;
//    private String netInterestAmount;
//    private String interestTaxAmount;
//    private String receiverAccount;
//    private String usageText;
//    private List<InterestLadder> interestLadder;

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(translateAccountType(), accountId, new Amount(currency, balance))
                .setAccountNumber(bbanFormatted)
                .setName(accountNickname)
                .addIdentifier(new IbanIdentifier(bic, accountNumber))
                .addIdentifier(new FinnishIdentifier(bban))
                .build();
    }

    public boolean isTransactionalAccount() {
        AccountTypes type = translateAccountType();
        return type == AccountTypes.CHECKING || type == AccountTypes.SAVINGS || type == AccountTypes.OTHER;
    }

    private AccountTypes translateAccountType() {
        if (AlandsBankenConstants.Fetcher.Account.SAVING.equalsIgnoreCase(usageType)) {
            return AccountTypes.SAVINGS;
        }

        switch (Strings.nullToEmpty(accountGroup).toLowerCase()) {
        case AlandsBankenConstants.Fetcher.Account.LOAN:
            return AccountTypes.LOAN;
        case AlandsBankenConstants.Fetcher.Account.CHECK:
            return AccountTypes.CHECKING;
        case AlandsBankenConstants.Fetcher.Account.INVESTMENT:
            return AccountTypes.INVESTMENT;
        default:
            return AccountTypes.OTHER;
        }
    }

    public boolean isInvestmentAccount() {
        AccountTypes type = translateAccountType();
        return type == AccountTypes.INVESTMENT;
    }

    public InvestmentAccount toInvestmentAccount(Portfolio portfolio) {
        return InvestmentAccount.builder(accountId, new Amount(currency, balance))
                .setAccountNumber(bbanFormatted)
                .setName(accountNickname)
                .addIdentifier(new IbanIdentifier(bic, accountNumber))
                .addIdentifier(new FinnishIdentifier(bban))
                .setBankIdentifier(accountId)
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    public boolean isKnownPortfolioType() {
        return AlandsBankenConstants.PORTFOLIO_TYPES.containsKey(accountType);
    }

    public Portfolio.Type getPortfolioType() {
        return AlandsBankenConstants.PORTFOLIO_TYPES.getOrDefault(accountType, Portfolio.Type.OTHER);
    }

    public boolean isLoan() {
        return AlandsBankenConstants.Fetcher.Account.LOAN.equalsIgnoreCase(accountGroup);
    }

    public boolean isKnownLoanType() {
        return AlandsBankenConstants.LOAN_TYPES.containsKey(accountType);
    }

    public LoanDetails.Type getLoanType() {
        return AlandsBankenConstants.LOAN_TYPES.getOrDefault(accountType, LoanDetails.Type.OTHER);
    }

    public LoanAccount toLoanAccount(LoanDetailsEntity loanDetailsEntity) {
        return LoanAccount.builder(accountNumber, new Amount(currency, balance))
                .setAccountNumber(bbanFormatted)
                .setName(accountNickname)
                .setInterestRate(interestRate)
                .setBankIdentifier(accountId)
                .setDetails(LoanDetails.builder()
                        .setName(accountNickname)
                        .setLoanNumber(bban)
                        .setInitialBalance(new Amount(currency, loanDetailsEntity.getGrantedAmount()))
                        .setInitialDate(loanDetailsEntity.getOpeningDate())
                        .setNextDayOfTermsChange(loanDetailsEntity.getNextInterestAdjustmentDate())
                        .setType(getLoanType())
                        .build())
                .build();
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
}
