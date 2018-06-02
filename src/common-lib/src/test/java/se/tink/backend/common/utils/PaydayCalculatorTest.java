package se.tink.backend.common.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.abnamro.utils.AbnAmroTestDataUtils;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class PaydayCalculatorTest {

    private static SECategories categoryConfiguration;
    private static ImmutableMap<String, Category> categories;

    @BeforeClass
    public static void beforeClass() {
        categoryConfiguration = new SECategories();

        categories = ImmutableMap.of(SECategories.Codes.INCOME_SALARY_OTHER,
                new Category("Dummy", "Dummy", SECategories.Codes.INCOME_SALARY_OTHER, 1, CategoryTypes.EXPENSES),
                SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED
                , new Category("Dummy", "Dummy", SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED, 2,
                        CategoryTypes.EXPENSES));
    }

    @Test
    public void nullTransactionInputShouldNotHavePaydate() {
        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, null);

        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE)).isNull();
        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.ADVANCED)).isNull();
    }

    @Test
    public void emptyTransactionInputShouldNotHavePaydate() {

        List<Transaction> transactions = Lists.newArrayList();

        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, transactions);

        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE)).isNull();
        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.ADVANCED)).isNull();
    }

    @Test
    public void nullTransactionInputShouldNotHaveLastSalaryDate() {
        assertThat(new PaydayCalculator(categoryConfiguration, categories, null).detectLastSalaryDate()).isNull();
    }

    @Test
    public void emptyTransactionInputShouldNotHaveLastSalaryDate() {

        List<Transaction> transactions = Lists.newArrayList();

        assertThat(new PaydayCalculator(categoryConfiguration, categories, transactions).detectLastSalaryDate()).isNull();
    }

    @Test
    public void noSalaryTransactionShouldNotHaveLastSalaryDate() {

        Transaction t1 = new Transaction();
        t1.setOriginalDate(new Date());
        t1.setCategory(categories.get(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED));
        t1.setAmount(-1000.);

        List<Transaction> transactions = Lists.newArrayList(t1);

        assertThat(new PaydayCalculator(categoryConfiguration, categories, transactions).detectLastSalaryDate()).isNull();
    }

    @Test
    public void oneSalaryTransactionShouldHaveLastSalaryDate() {

        Transaction t1 = new Transaction();
        t1.setDate(new Date());
        t1.setCategory(categories.get(SECategories.Codes.INCOME_SALARY_OTHER));
        t1.setAmount(1000.);

        List<Transaction> transactions = Lists.newArrayList(t1);

        assertThat(new PaydayCalculator(categoryConfiguration, categories, transactions).detectLastSalaryDate()).isNotNull();
    }

    @Test
    public void shouldDetectSalaryFromWeekends() {

        List<String> dates = Lists.newArrayList();

        // Salary on the 20th every month but the 20th of February and March are on weekends

        dates.add("2016-01-20"); // Wednesday
        dates.add("2016-02-19"); // 2016-02-20 is a Saturday
        dates.add("2016-03-18"); // 2016-03-20 is a Sunday
        dates.add("2016-04-20"); // Wednesday

        List<Transaction> transactions = Lists.newArrayList();

        for (String date : dates) {
            transactions.add(createTransaction(date));
        }

        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, transactions);

        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE)).isNull();
        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.ADVANCED)).isEqualTo(20);
    }

    @Test
    public void shouldDetectNormalPayday() {

        List<String> dates = Lists.newArrayList();

        // Salary on the 25th every month

        dates.add("2016-05-25"); // Wednesday
        dates.add("2016-04-25"); // Monday
        dates.add("2016-03-25"); // Friday

        List<Transaction> transactions = Lists.newArrayList();

        for (String date : dates) {
            transactions.add(createTransaction(date));
        }

        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, transactions);

        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE)).isEqualTo(25);
        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.ADVANCED)).isEqualTo(25);
    }

    @Test
    public void shouldCalculateWithAllDays() {

        List<Transaction> transactions = Lists.newArrayList();

        for (int i = 0; i <= 31; i++) {
            Transaction transaction = createTransaction("2016-01-01");

            transaction.setDate(new DateTime(transaction.getDate()).plusDays(i).toDate());

            transactions.add(transaction);
        }

        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, transactions);

        // Should not detect a pay date since we require a value > 2
        assertThat(calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE)).isNull();
    }

    private Transaction createTransaction(String date) {
        Transaction transaction = new Transaction();
        transaction.setDate(DateUtils.parseDate(date));
        transaction.setCategory(categories.get(SECategories.Codes.INCOME_SALARY_OTHER));

        return transaction;
    }

    @Test
    @Ignore
    public void performanceTest() {

        // Load up some dummy test data

        List<Account> accounts = AbnAmroTestDataUtils.getTestAccounts(new Credentials());

        List<Transaction> transactions = AbnAmroTestDataUtils.getTestTransactions(accounts);

        for (Transaction transaction : transactions) {
            transaction.setOriginalDate(transaction.getDate());

            // Categorize all incomes above 1500 EUR as salary
            if (transaction.getAmount() > 1500) {
                transaction.setCategory(categories.get(SECategories.Codes.INCOME_SALARY_OTHER));
            } else {
                transaction.setCategory(categories.get(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED));
            }
        }

        long startTime = System.currentTimeMillis();
        long maxTestTime = TimeUnit.SECONDS.toMillis(5);

        int iterations = 0;

        PaydayCalculator calculator = new PaydayCalculator(categoryConfiguration, categories, transactions);

        while ((System.currentTimeMillis() - startTime) < maxTestTime) {

            calculator.detectPayday(PaydayCalculator.CalculationMode.SIMPLE);

            iterations++;
        }

        double average = maxTestTime / (double) iterations;

        System.out.println(String.format("Iterations: %,d Average: %,f ms", iterations, average));
    }
}
