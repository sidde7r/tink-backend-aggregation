package se.tink.backend.common.workers.statistics.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.utils.AccountBalanceUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;

/**
 * Calculates the estimated account balance based on fixed data-points and a
 * transaction trail.
 */
public class AccountBalanceHistoryCalculator {

    private static final LogUtils log = new LogUtils(AccountBalanceHistoryCalculator.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public AccountBalanceHistoryCalculator() {
        
    }

    /**
     * Calculates account balance history based on actual account balance entries and transactions. The algorithm starts
     * at the first account balance entry and calculates account balance entries forward in time until today. If there
     * are transactions before the first actual account balance entry date, the algorithm goes back in time to the first
     * transaction date.
     */
    public List<AccountBalance> calculate(Account account, List<Transaction> transactions,
            List<AccountBalance> accountBalanceHistory, boolean accountIsDisabled) {

        if (log.isDebugEnabled()) {
            try {
                log.debug(account.getUserId(), account.getCredentialsId(),
                        "Processing: " + mapper.writeValueAsString(account));
            } catch (JsonProcessingException e) {
                log.warn(account.getUserId(), account.getCredentialsId(),
                        String.format("[accountId:%s] Processing.", account.getId()), e);
            }
        }

        AccountBalance accountBalanceRightNow = AccountBalanceUtils.createEntry(account);
        
        // No account balance history was supplied.
        if (accountBalanceHistory == null || accountBalanceHistory.isEmpty()) {

            if (accountIsDisabled) {
                // Since we don't want to base the calculations on the current account balance for disabled accounts
                // (since the current account balance is not actually current), return empty results.

                log.warn(
                        account.getUserId(),
                        String.format(
                                "[accountId:%s] No account balance history available for disabled account; return empty results.",
                                account.getId()));
                
                return Lists.newArrayList();
            } else {
                // Populate the account balance history with the current account balance (as of it would be up to date
                // right now), to base the calculations of that.

                log.warn(account.getUserId(),
                        String.format(
                                "[accountId:%s] No account balance history available; use current account balance.",
                                account.getId()));

                accountBalanceHistory = Lists.newArrayList(accountBalanceRightNow);
            }
        }
        
        ImmutableMap<Integer, AccountBalance> accountBalanceByDate = Maps.uniqueIndex(
                accountBalanceHistory, AccountBalance::getDate);
        
        // If the account is enabled and the account balances don't contain the balance right now; add it!
        if (!accountIsDisabled && !accountBalanceByDate.containsKey(accountBalanceRightNow.getDate())) {
            accountBalanceByDate = ImmutableMap
                    .<Integer, AccountBalance>builder()
                    .putAll(accountBalanceByDate)
                    .put(accountBalanceRightNow.getDate(), accountBalanceRightNow)
                    .build();
        }
        
        ImmutableListMultimap<Integer, Transaction> transactionsByDate = Multimaps.index(transactions,
                transaction -> DateUtils.toInteger(transaction.getOriginalDate()));
        
        final Integer firstAccountBalanceDate = Collections.min(accountBalanceByDate.keySet());
        final Integer lastAccountBalanceDate = Collections.max(accountBalanceByDate.keySet());
        
        final Integer startDate;
        {
            if (transactionsByDate.isEmpty()) {
                startDate = firstAccountBalanceDate;
            } else {
                Integer firstTransactionDate = Collections.min(transactionsByDate.keySet());
                startDate = Math.min(firstAccountBalanceDate, firstTransactionDate);
            }
        }
        
        final Integer endDate = Math.max(lastAccountBalanceDate, DateUtils.toInteger(DateUtils.getToday()));
        
        int days = DateUtils.daysBetween(DateUtils.fromInteger(startDate), DateUtils.fromInteger(endDate)) + 1;
        
        List<AccountBalance> fullAccountBalanceHistory = Lists.newArrayListWithCapacity(days);
        
        
        Integer indexDate;
        Calendar indexCalendar;
        AccountBalance previousAccountBalance = null;

        
        // Calculate account balance (forward) from the first account balance entry to the last account balance entry.
        
        indexDate = firstAccountBalanceDate;
        indexCalendar = DateUtils.getCalendar(DateUtils.fromInteger(indexDate));
        
        while (indexDate <= endDate) {
            
            AccountBalance accountBalance;
            
            AccountBalance actualAccountBalance = accountBalanceByDate.get(indexDate);
            
            if (actualAccountBalance == null) {
                
                accountBalance = new AccountBalance();
                accountBalance.setUserId(previousAccountBalance.getUserId());
                accountBalance.setAccountId(previousAccountBalance.getAccountId());
                accountBalance.setDate(indexDate);

                List<Transaction> transactionsAtDate = transactionsByDate.get(indexDate);
                
                if (transactionsAtDate.isEmpty()) {
                    if (accountIsDisabled) {
                        // PRIO 4: Use the balance of the previous entry.
                        accountBalance.setBalance(0d);
                    } else {
                        // PRIO 3: Use the balance of the previous entry.
                        accountBalance.setBalance(previousAccountBalance.getBalance());
                    }
                } else {
                    // PRIO 2: Offset the balance of the previous entry with the sum of the day's transactions.
                    accountBalance.setBalance(previousAccountBalance.getBalance() + sumAmounts(transactionsAtDate));
                }
            } else {
                // PRIO 1: Actual account balance entry.
                accountBalance = actualAccountBalance;
            }
            
            fullAccountBalanceHistory.add(accountBalance);
            
            previousAccountBalance = accountBalance;
            
            // Increment index.
            indexCalendar.add(Calendar.DAY_OF_YEAR, 1);
            indexDate = DateUtils.toInteger(indexCalendar);
        } 
        
        
        // Calculate account balance (backwards) from the first account balance entry to the date of the first transaction.
        
        Integer transactionIndexDate;
        indexDate = firstAccountBalanceDate;
        indexCalendar = DateUtils.getCalendar(DateUtils.fromInteger(indexDate));
        previousAccountBalance = accountBalanceByDate.get(indexDate);
        
        while (indexDate > startDate) {
            
            transactionIndexDate = indexDate;
            
            // Decrement index.
            indexCalendar.add(Calendar.DAY_OF_YEAR, -1);
            indexDate = DateUtils.toInteger(indexCalendar);
            
            AccountBalance accountBalance = new AccountBalance();
            accountBalance.setUserId(previousAccountBalance.getUserId());
            accountBalance.setAccountId(previousAccountBalance.getAccountId());
            accountBalance.setDate(indexDate);
            
            List<Transaction> transactionsAtDate = transactionsByDate.get(transactionIndexDate);
            
            if (transactionsAtDate.isEmpty()) {
                accountBalance.setBalance(previousAccountBalance.getBalance());
            } else {
                accountBalance.setBalance(previousAccountBalance.getBalance() - sumAmounts(transactionsAtDate));
            }
            
            fullAccountBalanceHistory.add(accountBalance);
            
            previousAccountBalance = accountBalance;
        }

        return fullAccountBalanceHistory.stream().sorted(Orderings.ACCOUNT_BALANCE_HISTORY_ORDERING)
                .collect(Collectors.toList());
    }

    private static double sumAmounts(Iterable<Transaction> transactions) {
        double sum = 0;
        
        for (Transaction transaction : transactions) {
            sum += transaction.getAmount();
        }
        
        return sum;
    }
}
