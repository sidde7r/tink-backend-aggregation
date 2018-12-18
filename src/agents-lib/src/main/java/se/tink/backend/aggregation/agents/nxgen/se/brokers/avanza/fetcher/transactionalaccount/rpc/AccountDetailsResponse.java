package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.CurrencyAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.MAPPERS;

@JsonObject
public class AccountDetailsResponse {
    private List<CurrencyAccountEntity> currencyAccounts;
    private String accountId;
    private String accountType;
    private String accountTypeName;
    private String clearingNumber;
    private String courtageClass;
    private String externalActor;
    private boolean allowMonthlySaving;
    private boolean depositable;
    private boolean instrumentTransferPossible;
    private boolean internalTransferPossible;
    private boolean jointlyOwned;
    private boolean overMortgaged;
    private boolean overdrawn;
    private boolean withdrawable;
    private double accruedInterest;
    private double availableSuperLoanAmount;
    private double buyingPower;
    private double creditAfterInterest;
    private double creditLimit;
    private double forwardBalance;
    private double interestRate;
    private double ownCapital;
    private double performance;
    private double performancePercent;
    private double performanceSinceOneMonth;
    private double performanceSinceOneMonthPercent;
    private double performanceSinceOneWeek;
    private double performanceSinceOneWeekPercent;
    private double performanceSinceOneYear;
    private double performanceSinceOneYearPercent;
    private double performanceSinceSixMonths;
    private double performanceSinceSixMonthsPercent;
    private double performanceSinceThreeMonths;
    private double performanceSinceThreeMonthsPercent;
    private double performanceSinceThreeYears;
    private double performanceSinceThreeYearsPercent;
    private double reservedAmount;
    private double sharpeRatio;
    private double standardDeviation;
    private double totalBalance;
    private double totalCollateralValue;
    private double totalPositionsValue;
    private double totalProfit;
    private double totalProfitPercent;
    private int numberOfDeals;
    private int numberOfIntradayTransfers;
    private int numberOfOrders;
    private int numberOfTransfers;

    public List<CurrencyAccountEntity> getCurrencyAccounts() {
        return Optional.ofNullable(currencyAccounts).orElse(Collections.emptyList());
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getCourtageClass() {
        return courtageClass;
    }

    public String getExternalActor() {
        return externalActor;
    }

    public boolean isAllowMonthlySaving() {
        return allowMonthlySaving;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public boolean isInstrumentTransferPossible() {
        return instrumentTransferPossible;
    }

    public boolean isInternalTransferPossible() {
        return internalTransferPossible;
    }

    public boolean isJointlyOwned() {
        return jointlyOwned;
    }

    public boolean isOverMortgaged() {
        return overMortgaged;
    }

    public boolean isOverdrawn() {
        return overdrawn;
    }

    public boolean isWithdrawable() {
        return withdrawable;
    }

    public double getAccruedInterest() {
        return accruedInterest;
    }

    public double getAvailableSuperLoanAmount() {
        return availableSuperLoanAmount;
    }

    public double getBuyingPower() {
        return buyingPower;
    }

    public double getCreditAfterInterest() {
        return creditAfterInterest;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public double getForwardBalance() {
        return forwardBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getOwnCapital() {
        return ownCapital;
    }

    public double getPerformance() {
        return performance;
    }

    public double getPerformancePercent() {
        return performancePercent;
    }

    public double getPerformanceSinceOneMonth() {
        return performanceSinceOneMonth;
    }

    public double getPerformanceSinceOneMonthPercent() {
        return performanceSinceOneMonthPercent;
    }

    public double getPerformanceSinceOneWeek() {
        return performanceSinceOneWeek;
    }

    public double getPerformanceSinceOneWeekPercent() {
        return performanceSinceOneWeekPercent;
    }

    public double getPerformanceSinceOneYear() {
        return performanceSinceOneYear;
    }

    public double getPerformanceSinceOneYearPercent() {
        return performanceSinceOneYearPercent;
    }

    public double getPerformanceSinceSixMonths() {
        return performanceSinceSixMonths;
    }

    public double getPerformanceSinceSixMonthsPercent() {
        return performanceSinceSixMonthsPercent;
    }

    public double getPerformanceSinceThreeMonths() {
        return performanceSinceThreeMonths;
    }

    public double getPerformanceSinceThreeMonthsPercent() {
        return performanceSinceThreeMonthsPercent;
    }

    public double getPerformanceSinceThreeYears() {
        return performanceSinceThreeYears;
    }

    public double getPerformanceSinceThreeYearsPercent() {
        return performanceSinceThreeYearsPercent;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getTotalCollateralValue() {
        return totalCollateralValue;
    }

    public double getTotalPositionsValue() {
        return totalPositionsValue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public int getNumberOfDeals() {
        return numberOfDeals;
    }

    public int getNumberOfIntradayTransfers() {
        return numberOfIntradayTransfers;
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
    }

    public int getNumberOfTransfers() {
        return numberOfTransfers;
    }

    public AccountTypes toTinkAccountType() {
        return MAPPERS.inferAccountType(accountType).orElse(AccountTypes.OTHER);
    }

    public <T extends Account> T toTinkAccount(HolderName holderName, Class<T> type) {
        final String accountName =
                Strings.isNullOrEmpty(externalActor) ? accountType : accountType + externalActor;

        final Account account =
                Account.builder(toTinkAccountType(), accountId)
                        .setAccountNumber(accountId)
                        .setName(accountName)
                        .setHolderName(holderName)
                        .setBalance(new Amount("SEK", ownCapital))
                        .setBankIdentifier(accountId)
                        .build();

        return type.cast(account);
    }

    public TransactionalAccount toTinkAccount(HolderName holderName) {
        return toTinkAccount(holderName, TransactionalAccount.class);
    }
}
