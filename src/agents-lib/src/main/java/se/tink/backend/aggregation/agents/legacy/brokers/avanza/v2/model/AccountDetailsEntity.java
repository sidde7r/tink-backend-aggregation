package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.MAPPERS;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetailsEntity {
    private String accountType;
    private String accountId;
    private boolean jointlyOwned;
    private String clearingNumber;
    private String accountTypeName;
    private double interestRate;
    private double numberOfOrders;
    private double numberOfDeals;
    private double availableSuperLoanAmount;
    private double creditLimit;
    private double forwardBalance;
    private double reservedAmount;
    private double totalCollateralValue;
    private double totalPositionsValue;
    private double buyingPower;
    private double performance;
    private double performancePercent;
    private double accruedInterest;
    private double totalBalance;
    private double ownCapital;
    private List<CurrencyAccountEntity> currencyAccounts;
    private double creditAccountBalance;
    private String creditAccountId;
    private String ownerName;
    private CreditedAccountEntity creditedAccount;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isJointlyOwned() {
        return jointlyOwned;
    }

    public void setJointlyOwned(boolean jointlyOwned) {
        this.jointlyOwned = jointlyOwned;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(double numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    public double getNumberOfDeals() {
        return numberOfDeals;
    }

    public void setNumberOfDeals(double numberOfDeals) {
        this.numberOfDeals = numberOfDeals;
    }

    public double getAvailableSuperLoanAmount() {
        return availableSuperLoanAmount;
    }

    public void setAvailableSuperLoanAmount(double availableSuperLoanAmount) {
        this.availableSuperLoanAmount = availableSuperLoanAmount;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getForwardBalance() {
        return forwardBalance;
    }

    public void setForwardBalance(double forwardBalance) {
        this.forwardBalance = forwardBalance;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public double getTotalCollateralValue() {
        return totalCollateralValue;
    }

    public void setTotalCollateralValue(double totalCollateralValue) {
        this.totalCollateralValue = totalCollateralValue;
    }

    public double getTotalPositionsValue() {
        return totalPositionsValue;
    }

    public void setTotalPositionsValue(double totalPositionsValue) {
        this.totalPositionsValue = totalPositionsValue;
    }

    public double getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(double buyingPower) {
        this.buyingPower = buyingPower;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getPerformancePercent() {
        return performancePercent;
    }

    public void setPerformancePercent(double performancePercent) {
        this.performancePercent = performancePercent;
    }

    public double getAccruedInterest() {
        return accruedInterest;
    }

    public void setAccruedInterest(double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getOwnCapital() {
        return ownCapital;
    }

    public void setOwnCapital(double ownCapital) {
        this.ownCapital = ownCapital;
    }

    public List<CurrencyAccountEntity> getCurrencyAccounts() {
        return currencyAccounts;
    }

    public void setCurrencyAccounts(List<CurrencyAccountEntity> currencyAccounts) {
        this.currencyAccounts = currencyAccounts;
    }

    public AccountTypes toTinkAccountType() {
        Optional<AccountTypes> accountType = MAPPERS.inferAccountType(getAccountType());
        return accountType.orElse(AccountTypes.OTHER);
    }

    public Account toAccount(AccountEntity accountEntity) {
        Account account = new Account();

        account.setBankId(accountId);

        // Validate account numbers.
        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches("[1-9][0-9]*"),
                "Unexpected account.bankid '%s'. Reformatted?",
                account.getBankId());

        if (Strings.isNullOrEmpty(clearingNumber)) {
            account.setAccountNumber(accountId);
        } else {
            account.setAccountNumber(clearingNumber + "-" + accountId);
        }

        if (!Strings.isNullOrEmpty(accountEntity.getName())
                && !Objects.equals(accountEntity.getAccountId(), accountEntity.getName())) {
            account.setName(accountEntity.getName());
        } else {
            account.setName(accountTypeName);
        }

        account.setBalance(ownCapital);
        account.setType(toTinkAccountType());

        account.putIdentifier(new SwedishIdentifier(account.getAccountNumber()));

        return account;
    }

    public double getCreditAccountBalance() {
        return creditAccountBalance;
    }

    public void setCreditAccountBalance(double creditAccountBalance) {
        this.creditAccountBalance = creditAccountBalance;
    }

    public String getCreditAccountId() {
        return creditAccountId;
    }

    public void setCreditAccountId(String creditAccountId) {
        this.creditAccountId = creditAccountId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public CreditedAccountEntity getCreditedAccount() {
        return creditedAccount;
    }

    public void setCreditedAccount(CreditedAccountEntity creditedAccount) {
        this.creditedAccount = creditedAccount;
    }
}
