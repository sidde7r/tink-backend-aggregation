package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.calculators.AvailableBalanceCalculator;
import se.tink.backend.aggregation.agents.balance.calculators.BookedBalanceCalculator;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@AllArgsConstructor
@RequiredArgsConstructor
public class AccountsBalancesUpdater {

    private final BookedBalanceCalculator bookedBalanceCalculator;
    private AvailableBalanceCalculator availableBalanceCalculator;

    public void updateAccountsBalancesByRunningCalculations(List<AccountData> accountsData) {
        try {
            for (AccountData accountData : accountsData) {
                Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances =
                        accountData.getAccount().getGranularAccountBalances();
                List<Transaction> transactions = accountData.getTransactions();
                Account account = accountData.getAccount();

                updateBookedBalanceByRunningCalculation(account, granularBalances, transactions);
                updateAvailableBalanceByRunningCalculation(granularBalances, transactions, account);
            }

        } catch (Exception e) {
            log.warn("[ACCOUNTS BALANCES UPDATER] Something went wrong", e);
        }
    }

    private void updateAvailableBalanceByRunningCalculation(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions,
            Account account) {
        ExactCurrencyAmount buggyAvailableBalance = account.getAvailableBalance();
        Optional<ExactCurrencyAmount> calculatedAvailableBalance =
                availableBalanceCalculator.calculateAvailableBalance(
                        granularBalances, transactions);
        calculatedAvailableBalance.ifPresent(account::setAvailableBalance);

        log.info(
                "[AVAILABLE BALANCE] Buggy {}, calculated {}",
                buggyAvailableBalance,
                calculatedAvailableBalance);
    }

    private void updateBookedBalanceByRunningCalculation(
            Account account,
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions) {
        ExactCurrencyAmount buggyBookedBalance = account.getExactBalance();
        Optional<ExactCurrencyAmount> calculatedBookedBalance =
                bookedBalanceCalculator.calculateBookedBalance(granularBalances, transactions);
        calculatedBookedBalance.ifPresent(
                balance -> {
                    account.setExactBalance(balance);
                    account.setBalance(balance.getDoubleValue());
                });

        log.info(
                "[BOOKED BALANCE] Buggy {}, calculated {}",
                buggyBookedBalance,
                calculatedBookedBalance);
    }
}
