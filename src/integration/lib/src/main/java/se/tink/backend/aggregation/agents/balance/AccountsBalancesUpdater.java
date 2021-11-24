package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AccountsBalancesUpdater {

    private final BookedBalanceCalculator bookedBalanceCalculator;
    private final AvailableBalanceCalculator availableBalanceCalculator;

    private static final EnumSet<AccountTypes> ACCOUNT_TYPES_SUPPORTING_BOOKED_CALCULATIONS =
            EnumSet.of(AccountTypes.CHECKING, AccountTypes.SAVINGS, AccountTypes.CREDIT_CARD);
    private static final EnumSet<AccountTypes> ACCOUNT_TYPES_SUPPORTING_AVAILABLE_CALCULATIONS =
            EnumSet.of(AccountTypes.CHECKING, AccountTypes.SAVINGS);

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
            log.warn("[ACCOUNTS BALANCES UPDATER] Something went wrong", e);
        }
    }

    private void updateBookedBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {

        if (ACCOUNT_TYPES_SUPPORTING_BOOKED_CALCULATIONS.contains(account.getType())) {
            log.info(
                "[ACCOUNTS BALANCES UPDATER] Found account type {}. Trying to run booked balance calculation", account.getType());

            ExactCurrencyAmount buggyBookedBalance = account.getExactBalance();
            Optional<ExactCurrencyAmount> calculatedBookedBalance =
                    bookedBalanceCalculator.calculateBookedBalance(granularBalances, transactions);
            calculatedBookedBalance.ifPresent(
                    balance -> {
                        account.setExactBalance(balance);
                        account.setBalance(balance.getDoubleValue());
                    });

            log.info(
                    "[ACCOUNTS BALANCES UPDATER] Buggy booked balance potentially replaced by calculated: {} -> {}",
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
                "[ACCOUNTS BALANCES UPDATER] Found account type {}. Trying to run available balance calculation", account.getType());

            ExactCurrencyAmount buggyAvailableBalance = account.getAvailableBalance();
            Optional<ExactCurrencyAmount> calculatedAvailableBalance =
                    availableBalanceCalculator.calculateAvailableBalance(
                            granularBalances, transactions);
            calculatedAvailableBalance.ifPresent(account::setAvailableBalance);

            log.info(
                    "[ACCOUNTS BALANCES UPDATER] Available balance potentially replaced by calculated: {} -> {}",
                    buggyAvailableBalance,
                    calculatedAvailableBalance);
        }
    }

}
