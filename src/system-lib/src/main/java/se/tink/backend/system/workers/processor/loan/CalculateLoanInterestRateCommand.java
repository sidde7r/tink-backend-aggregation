package se.tink.backend.system.workers.processor.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.common.concurrency.ListenableExecutor;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * This calculation for Danske Bank might not work if user have done manual transfers back and forth to loan account
 */
public class CalculateLoanInterestRateCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(CalculateLoanInterestRateCommand.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TransactionProcessorContext context;

    private LoanDAO loanDAO;
    private ListMultimap<String, Loan> loansToProcessByAccount;
    private ListMultimap<String, Loan> mutableLoanDataByAccount;

    private ListMultimap<String, String> daysWithInterestPaymentByAccount;
    private Map<String, ListMultimap<String, Transaction>> transactionsByDayByAccount;

    private static final ImmutableSet<String> APPLICABLE_PROVIDERS = ImmutableSet.of("danskebank", "danskebank-bankid");
    private static final Ordering<String> NATURAL_STRING = Ordering.natural();
    private static final Ordering<Loan> NATURAL_LOAN = Ordering.natural();

    private final TargetProductsRunnableFactory targetProductsRunnableFactory;
    private ListenableExecutor asyncExecutor;

    @VisibleForTesting
    /*package*/ CalculateLoanInterestRateCommand(
            TransactionProcessorContext context,
            LoanDAO loanDAO,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            ListenableExecutor asyncExecutor
    ) {
        this.context = context;
        this.loanDAO = loanDAO;
        this.targetProductsRunnableFactory = targetProductsRunnableFactory;
        this.asyncExecutor = asyncExecutor;

        loansToProcessByAccount = ArrayListMultimap.create();
        transactionsByDayByAccount = Maps.newHashMap();
        daysWithInterestPaymentByAccount = ArrayListMultimap.create();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    public static Optional<CalculateLoanInterestRateCommand> build(
            Provider provider,
            TransactionProcessorContext context,
            LoanDAO loanDAO,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            ListenableExecutor asyncExecutor) {
        
        if (!APPLICABLE_PROVIDERS.contains(provider.getName())) {
            return Optional.empty();
        }
        return Optional.of(new CalculateLoanInterestRateCommand(context, loanDAO, targetProductsRunnableFactory,
                asyncExecutor));
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        ImmutableListMultimap<String, Loan> loanDataByAccount = context.getUserData().getLoanDataByAccount();
        mutableLoanDataByAccount = ArrayListMultimap.create(loanDataByAccount);

        for (String accountId : loanDataByAccount.keySet()) {
            List<Loan> loans = loanDataByAccount.get(accountId);
            if (loans.size() > 0) {
                Loan mostRecent = NATURAL_LOAN.max(loans);
                loansToProcessByAccount.put(accountId, mostRecent);
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        if (!loansToProcessByAccount.containsKey(transaction.getAccountId())) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        String date = ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getOriginalDate());

        if ("Ränta".equals(transaction.getOriginalDescription())) {
            daysWithInterestPaymentByAccount.put(transaction.getAccountId(), date);
        }

        if (!transactionsByDayByAccount.containsKey(transaction.getAccountId())) {
            ListMultimap<String, Transaction> multimap = ArrayListMultimap.create();
            transactionsByDayByAccount.put(transaction.getAccountId(), multimap);
        }

        transactionsByDayByAccount.get(transaction.getAccountId()).put(date, transaction);

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {

        for (String accountId : daysWithInterestPaymentByAccount.keySet()) {

            List<String> datesWithRatePayments = daysWithInterestPaymentByAccount.get(accountId);
            ListMultimap<String, Transaction> transactionsByDay = transactionsByDayByAccount.get(accountId);

            if (datesWithRatePayments == null || datesWithRatePayments.size() == 0 ||
                    transactionsByDay == null || transactionsByDay.size() == 0) {
                continue;
            }

            String lastDate = NATURAL_STRING.max(datesWithRatePayments);

            List<Transaction> transactions = transactionsByDay.get(lastDate);

            double interestAmount = 0;
            double transferAmount = 0;
            double otherExpenses = 0;

            for (Transaction t : transactions) {
                if ("ränta".equals(t.getOriginalDescription().toLowerCase())) {
                    interestAmount = t.getOriginalAmount();
                } else if ("betalning".equals(t.getOriginalDescription().toLowerCase()) && t.getOriginalAmount() > 0) {
                    transferAmount = t.getOriginalAmount();
                } else if (t.getOriginalAmount() < 0) {
                    otherExpenses += t.getOriginalAmount();
                } else {
                    // TODO This is where other incomes ( amount > 0 ) could be filled out
                    // TODO Reason for not taking those into account now is that it is very unclear how
                    // TODO Danske does that calculation. Also it is most likely a very uncommon case.
                }
            }

            if (transferAmount > 0 && interestAmount < 0) {
                List<Loan> loans = loansToProcessByAccount.get(accountId);
                Loan mostRecent = NATURAL_LOAN.max(loans);

                // transferAmount is positive, interestAmount and otherExpenses negative
                double amortized = transferAmount + interestAmount + otherExpenses;
                // Rate was calculated before this month's amortization
                BigDecimal loanBalance = new BigDecimal(mostRecent.getBalance() - amortized);
                BigDecimal rateDays;

                BigDecimal daysInYear;
                if (mostRecent.getType() == Loan.Type.MORTGAGE) {
                    daysInYear = new BigDecimal(360);
                    rateDays = new BigDecimal(InterestRateDays.DanskeBank.getMortgageInterestRateDays(lastDate));
                } else {
                    daysInYear = new BigDecimal(365);
                    rateDays = new BigDecimal(InterestRateDays.DanskeBank.getBlancoInterestRateDays(lastDate));
                }

                MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

                BigDecimal rate = new BigDecimal(interestAmount)
                        .multiply(daysInYear)
                        .divide(loanBalance, mc)
                        .divide(rateDays, mc)
                        .setScale(4, RoundingMode.HALF_UP);

                Loan newLoan = new Loan(mostRecent);
                newLoan.setInterest(rate.doubleValue());
                try {
                    //Add the transactions the calculation was based on as a serialized response
                    newLoan.setSerializedLoanResponse(MAPPER.writeValueAsString(transactions));
                } catch (Exception e) {
                    log.warn(context.getUser().getId(), "Wasn't able to add serialized response: " + e.getMessage());
                }

                mutableLoanDataByAccount.get(accountId).add(newLoan);
                if (loanDAO.saveIfUpdated(newLoan)) {
                    // If the loan was updated, it might change whether the user qualifies (or disqualified) for a
                    // product.
                    Runnable runnable = targetProductsRunnableFactory.createRunnable(context.getUser());
                    if (runnable != null) {
                        asyncExecutor.execute(runnable);
                    }
                }
            }
        }

        context.getUserData().setLoanDataByAccount(
                ImmutableListMultimap.<String, Loan>builder().putAll(mutableLoanDataByAccount.entries()).build());
    }

}
