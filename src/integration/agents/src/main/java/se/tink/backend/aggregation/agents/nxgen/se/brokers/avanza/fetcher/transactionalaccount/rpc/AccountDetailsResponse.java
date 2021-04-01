package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.MAPPERS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.Currencies;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.CurrencyAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.LoanModuleBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountDetailsResponse {
    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDetailsResponse.class);

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

    @JsonProperty("interestRate")
    private BigDecimal interestRate;

    @JsonProperty("ownCapital")
    private BigDecimal ownCapital = null;

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

    @JsonProperty("totalBalanceDue")
    private BigDecimal totalBalanceDue = null;

    @JsonProperty("nextPaymentPrognosis")
    private BigDecimal nextPaymentPrognosis = null;

    @JsonProperty("remainingLoan")
    private BigDecimal remainingLoan = null;

    public List<CurrencyAccountEntity> getCurrencyAccounts() {
        return Optional.ofNullable(currencyAccounts).orElseGet(Collections::emptyList);
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

    public BigDecimal getOwnCapital() {
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

    public TransactionalAccountType toTinkAccountType() {
        return TransactionalAccountType.from(MAPPERS.inferAccountType(accountType).orElse(null))
                .orElse(null);
    }

    @JsonIgnore
    private String getAccountNumber() {
        return clearingNumber != null
                ? String.format("%s-%s", clearingNumber, accountId)
                : accountId;
    }

    @JsonIgnore
    private double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(interestRate).doubleValue();
    }

    @JsonIgnore
    public Optional<LoanAccount> toLoanAccount(String holderName) {
        return Optional.of(
                LoanAccount.nxBuilder()
                        .withLoanDetails(getLoanDetails())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(getAccountNumber())
                                        .withAccountNumber(getAccountNumber())
                                        .withAccountName(accountTypeName)
                                        .addIdentifier(new SwedishIdentifier(getAccountNumber()))
                                        .setProductName(accountType)
                                        .build())
                        .addHolderName(holderName)
                        .setApiIdentifier(accountId)
                        .build());
    }

    @JsonIgnore
    private LoanModule getLoanDetails() {
        LoanModuleBuildStep builder =
                LoanModule.builder()
                        .withType(MAPPERS.getLoanType(accountType).orElse(LoanDetails.Type.OTHER))
                        .withBalance(getLoanBalance())
                        .withInterestRate(getInterestRate());
        if (!Objects.isNull(remainingLoan)) {
            builder.setAmortized(new ExactCurrencyAmount(remainingLoan, Currencies.SEK));
        }

        return builder.build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getLoanBalance() {
        ExactCurrencyAmount result = getBalance();
        if (result.getExactValue().signum() != -1) {
            LOGGER.info("Loan account has positive balance");
            result = result.negate();
        } else {
            LOGGER.info("Loan account has negative balance");
        }
        return result;
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        final ExactCurrencyAmount result;
        if (!Objects.isNull(ownCapital)) {
            result = new ExactCurrencyAmount(ownCapital, Currencies.SEK);
        } else if (!Objects.isNull(totalBalanceDue)) {
            result = new ExactCurrencyAmount(totalBalanceDue, Currencies.SEK);
        } else {
            throw new IllegalStateException("Could not parse balance!");
        }
        return result;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String holderName) {
        final String accountName =
                Strings.isNullOrEmpty(externalActor) ? accountType : accountType + externalActor;
        final String accountNumber = getAccountNumber();

        return TransactionalAccount.nxBuilder()
                .withType(toTinkAccountType())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(ownCapital, "SEK")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(new SwedishIdentifier(accountId))
                                .build())
                .addHolderName(holderName)
                .setApiIdentifier(accountId)
                .build();
    }
}
