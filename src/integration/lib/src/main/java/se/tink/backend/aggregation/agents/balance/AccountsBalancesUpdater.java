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
import se.tink.backend.aggregation.agents.balance.calculators.AvailableBalanceCalculator;
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

    public final boolean dryRun;

    public AccountsBalancesUpdater(
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
            log.warn("[BALANCE CALCULATOR] Something went wrong", e);
        }
    }

    private void updateBookedBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {

        if (ACCOUNT_TYPES_SUPPORTING_BOOKED_CALCULATIONS.contains(account.getType())) {
            log.info(
                    "[BALANCE CALCULATOR] Found account type {}. Trying to run "
                            + "booked balance calculation",
                    account.getType());

            ExactCurrencyAmount buggyBookedBalance = account.getExactBalance();
            Optional<ExactCurrencyAmount> calculatedBookedBalance =
                    bookedBalanceCalculator.calculateBookedBalance(granularBalances, transactions);

            if (dryRun) {
                log.info(
                        "[BALANCE CALCULATOR] Dry run. Buggy booked balance would be "
                                + "replaced by calculated: {} -> {}",
                        buggyBookedBalance,
                        calculatedBookedBalance);
            }

            calculatedBookedBalance.ifPresent(
                    balance -> {
                        account.setExactBalance(balance);
                        account.setBalance(balance.getDoubleValue());
                    });

            log.info(
                    "[BALANCE CALCULATOR] Buggy booked balance was replaced "
                            + "by calculated: {} -> {}",
                    buggyBookedBalance,
                    calculatedBookedBalance);
        }
    }

    private void updateAvailableBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {

        if (ACCOUNT_TYPES_SUPPORTING_AVAILABLE_CALCULATIONS.contains(account.getType())) {
            log.info(
                    "[BALANCE CALCULATOR] Found account type {}. Trying to "
                            + "run available balance calculation",
                    account.getType());

            ExactCurrencyAmount buggyAvailableBalance = account.getAvailableBalance();
            Optional<ExactCurrencyAmount> calculatedAvailableBalance =
                    availableBalanceCalculator.calculateAvailableBalance(
                            granularBalances, transactions);

            if (dryRun) {
                log.info(
                        "[BALANCE CALCULATOR] Dry run. Buggy available balance would be "
                                + "replaced by calculated: {} -> {}",
                        buggyAvailableBalance,
                        calculatedAvailableBalance);
                return;
            }

            calculatedAvailableBalance.ifPresent(account::setAvailableBalance);

            log.info(
                    "[BALANCE CALCULATOR] Buggy available balance was replaced "
                            + "by calculated: {} -> {}",
                    buggyAvailableBalance,
                    calculatedAvailableBalance);
        }
    }
}
