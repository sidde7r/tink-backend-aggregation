package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.BalanceType;
import se.tink.backend.aggregation.agents.balance.AccountsBalanceUpdaterSummary.AccountsBalanceUpdaterSummaryBuilder;
import se.tink.backend.aggregation.agents.balance.calculators.AvailableBalanceCalculator;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculatorSummary;
import se.tink.backend.aggregation.agents.balance.calculators.BookedBalanceCalculator;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class AccountsBalancesUpdater {

    private static final EnumSet<AccountTypes> ACCOUNT_TYPES_SUPPORTING_BOOKED_CALCULATIONS =
            EnumSet.of(AccountTypes.CHECKING, AccountTypes.SAVINGS, AccountTypes.CREDIT_CARD);
    private static final EnumSet<AccountTypes> ACCOUNT_TYPES_SUPPORTING_AVAILABLE_CALCULATIONS =
            EnumSet.of(AccountTypes.CHECKING, AccountTypes.SAVINGS);

    private final BookedBalanceCalculator bookedBalanceCalculator;
    private final AvailableBalanceCalculator availableBalanceCalculator;

    private final boolean dryRun;

    private AccountsBalancesUpdater(
            BookedBalanceCalculator bookedBalanceCalculator,
            AvailableBalanceCalculator availableBalanceCalculator,
            boolean dryRun) {
        this.bookedBalanceCalculator = Objects.requireNonNull(bookedBalanceCalculator);
        this.availableBalanceCalculator = Objects.requireNonNull(availableBalanceCalculator);
        this.dryRun = dryRun;
    }

    public static AccountsBalancesUpdater createDryRunBalanceUpdater(
            BookedBalanceCalculator bookedBalanceCalculator,
            AvailableBalanceCalculator availableBalanceCalculator) {
        return new AccountsBalancesUpdater(
                bookedBalanceCalculator, availableBalanceCalculator, true);
    }

    public static AccountsBalancesUpdater createBalanceUpdater(
            BookedBalanceCalculator bookedBalanceCalculator,
            AvailableBalanceCalculator availableBalanceCalculator) {
        return new AccountsBalancesUpdater(
                bookedBalanceCalculator, availableBalanceCalculator, false);
    }

    public void updateAccountsBalancesByRunningCalculations(List<AccountData> accountsData) {
        try {
            accountsData.forEach(
                    data -> {
                        updateBookedBalanceByRunningCalculation(
                                data.getAccount(),
                                data.getAccount().getGranularAccountBalances(),
                                data.getTransactions());
                        updateAvailableBalanceByRunningCalculation(
                                data.getAccount(),
                                data.getAccount().getGranularAccountBalances(),
                                data.getTransactions());
                    });

        } catch (Exception e) {
            log.warn("[BALANCE UPDATER] Something went wrong", e);
        }
    }

    private void updateBookedBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {

        if (ACCOUNT_TYPES_SUPPORTING_BOOKED_CALCULATIONS.contains(account.getType())) {

            AccountsBalanceUpdaterSummaryBuilder summaryBuilder =
                    AccountsBalanceUpdaterSummary.builder()
                            .dryRun(dryRun)
                            .inputAccountType(account.getType())
                            .balanceTypeToCalculate(BalanceType.BOOKED_BALANCE)
                            .buggyBalance(account.getExactBalance().getExactValue())
                            .granularBalances(granularBalances);

            Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary>
                    calculatedBookedBalanceWithSummary =
                            bookedBalanceCalculator.calculateBookedBalance(
                                    granularBalances, transactions);
            summaryBuilder.balanceCalculatorSummary(calculatedBookedBalanceWithSummary.getRight());

            Optional<ExactCurrencyAmount> calculatedBookedBalance =
                    calculatedBookedBalanceWithSummary.getLeft();

            if (dryRun) {
                log.info(summaryBuilder.build().prettyPrint());
                return;
            }

            calculatedBookedBalance.ifPresent(
                    balance -> {
                        account.setExactBalance(balance);
                        account.setBalance(balance.getDoubleValue());
                        summaryBuilder.calculatedBalance(balance.getExactValue());
                    });

            log.info(summaryBuilder.build().prettyPrint());
        }
    }

    private void updateAvailableBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {

        if (ACCOUNT_TYPES_SUPPORTING_AVAILABLE_CALCULATIONS.contains(account.getType())) {
            AccountsBalanceUpdaterSummaryBuilder summaryBuilder =
                    AccountsBalanceUpdaterSummary.builder()
                            .dryRun(dryRun)
                            .inputAccountType(account.getType())
                            .balanceTypeToCalculate(BalanceType.AVAILABLE_BALANCE)
                            .buggyBalance(account.getAvailableBalance().getExactValue())
                            .granularBalances(granularBalances);

            Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary>
                    calculatedAvailableBalanceWithSummary =
                            availableBalanceCalculator.calculateAvailableBalance(
                                    granularBalances, transactions);

            Optional<ExactCurrencyAmount> calculatedAvailableBalance =
                    calculatedAvailableBalanceWithSummary.getLeft();

            summaryBuilder.balanceCalculatorSummary(
                    calculatedAvailableBalanceWithSummary.getRight());

            if (dryRun) {
                log.info(summaryBuilder.build().prettyPrint());
                return;
            }

            calculatedAvailableBalance.ifPresent(
                    balance -> {
                        account.setAvailableBalance(balance);
                        summaryBuilder.calculatedBalance(balance.getExactValue());
                    });

            log.info(summaryBuilder.build().prettyPrint());
        }
    }
}
