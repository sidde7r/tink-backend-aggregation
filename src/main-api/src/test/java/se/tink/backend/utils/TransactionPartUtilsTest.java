package se.tink.backend.utils;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPart;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionPartUtilsTest {

    @Test
    public void linkSingleIncomeToExpense() {
        Category transferCategory = new Category();
        transferCategory.setId(StringUtils.generateUUID());
        transferCategory.setType(CategoryTypes.TRANSFERS);

        Transaction expense = new Transaction();
        expense.setAmount(-1000.);
        expense.setDescription("Riche");

        Transaction income = new Transaction();
        income.setAmount(250.);
        income.setDescription("Swish");

        TransactionPartUtils.link(expense, income, transferCategory);

        assertThat(expense.getAmount()).isEqualTo(-1000); // Should never be changed
        assertThat(expense.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(-750)); // Amount left to link
        assertThat(expense.hasParts()).isTrue();
        assertThat(expense.getParts().get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-250));

        assertThat(income.getAmount()).isEqualTo(250);
        assertThat(income.getDispensableAmount()).isEqualTo(BigDecimal.ZERO);  // Nothing left to link
        assertThat(income.hasParts()).isTrue();
        assertThat(income.getParts().get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    public void linkMultipleIncomesToExpense() {
        Category transferCategory = new Category();
        transferCategory.setId(StringUtils.generateUUID());
        transferCategory.setType(CategoryTypes.TRANSFERS);

        Transaction expense = new Transaction();
        expense.setAmount(-1000.);
        expense.setDescription("Riche");

        Transaction income1 = new Transaction();
        income1.setAmount(250.);
        income1.setDescription("Swish");

        Transaction income2 = new Transaction();
        income2.setAmount(250.);
        income2.setDescription("Swish");

        TransactionPartUtils.link(expense, income1, transferCategory);
        TransactionPartUtils.link(expense, income2, transferCategory);

        assertThat(expense.getAmount()).isEqualTo(-1000); // Should never be changed
        assertThat(expense.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(-500)); // Amount left to link
        assertThat(expense.hasParts()).isTrue();
        assertThat(expense.getParts()).hasSize(2);

        assertThat(income1.getAmount()).isEqualTo(250);
        assertThat(income1.getDispensableAmount()).isEqualTo(BigDecimal.ZERO);  // Nothing left to link
        assertThat(income1.getParts()).hasSize(1);

        assertThat(income2.getAmount()).isEqualTo(250);
        assertThat(income2.getDispensableAmount()).isEqualTo(BigDecimal.ZERO);  // Nothing left to link
        assertThat(income2.getParts()).hasSize(1);
    }

    @Test
    public void linkIncomeToExpenseWithSameAmount() {
        Category transferCategory = new Category();
        transferCategory.setId(StringUtils.generateUUID());
        transferCategory.setType(CategoryTypes.TRANSFERS);

        Transaction expense = new Transaction();
        expense.setAmount(-1000.);
        expense.setDescription("H&M");

        Transaction income = new Transaction();
        income.setAmount(1000.);
        income.setDescription("Reimbursement from H&M");

        TransactionPartUtils.link(expense, income, transferCategory);

        assertThat(expense.getAmount()).isEqualTo(-1000);
        assertThat(expense.getDispensableAmount()).isEqualTo(BigDecimal.ZERO); // Nothing left to link
        assertThat(expense.getParts()).hasSize(1);

        assertThat(income.getAmount()).isEqualTo(1000);
        assertThat(income.getDispensableAmount()).isEqualTo(BigDecimal.ZERO); // Nothing left to link
        assertThat(income.getParts()).hasSize(1);
    }

    @Test
    public void linkExpenseToIncome() {
        Category transferCategory = new Category();
        transferCategory.setId(StringUtils.generateUUID());
        transferCategory.setType(CategoryTypes.TRANSFERS);

        Transaction expense = new Transaction();
        expense.setAmount(-100.);
        expense.setDescription("H&M");

        Transaction income = new Transaction();
        income.setAmount(1000.);
        income.setDescription("Reimbursement from H&M");

        TransactionPartUtils.link(expense, income, transferCategory);

        assertThat(expense.getAmount()).isEqualTo(-100);
        assertThat(expense.getDispensableAmount()).isEqualTo(BigDecimal.ZERO); // Nothing left to link
        assertThat(expense.getParts()).hasSize(1);

        assertThat(income.getAmount()).isEqualTo(1000);
        assertThat(income.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(income.getParts()).hasSize(1);
    }

    @Test
    public void unlinkSingleIncomeFromExpense() {
        Category transferCategory = new Category();
        transferCategory.setId(StringUtils.generateUUID());
        transferCategory.setType(CategoryTypes.TRANSFERS);

        Transaction expense = new Transaction();
        expense.setAmount(-1000.);
        expense.setDescription("Riche");

        Transaction income = new Transaction();
        income.setAmount(250.);
        income.setDescription("Swish");

        TransactionPartUtils.link(expense, income, transferCategory);

        assertThat(expense.getAmount()).isEqualTo(-1000); // Should never be changed
        assertThat(expense.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(-750)); // Amount left to link
        assertThat(expense.getParts()).hasSize(1);

        assertThat(income.getAmount()).isEqualTo(250);
        assertThat(income.getDispensableAmount()).isEqualTo(BigDecimal.ZERO);  // Nothing left to link
        assertThat(income.getParts()).hasSize(1);

        // Unlink the expense and income parts
        assertThat(expense.removePart(expense.getParts().get(0))).isTrue();
        assertThat(income.removePart(income.getParts().get(0))).isTrue();

        // Check that the state is the same as before the linking
        assertThat(expense.getParts()).isEmpty();
        assertThat(income.getParts()).isEmpty();

        assertThat(expense.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(-1000));
        assertThat(income.getDispensableAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    public void linkAndUnlinkTransactionParts() throws Exception {
        Transaction transaction = new Transaction();

        TransactionPart part1 = new TransactionPart();
        part1.setAmount(new BigDecimal(1000D));

        transaction.addPart(part1);

        // Create a copy that have the same id and verify that it will be removed.
        TransactionPart part2 = new TransactionPart();
        part2.setAmount(new BigDecimal(1000D));
        part2.setId(part1.getId());

        assertThat(transaction.removePart(part2)).isTrue();
        assertThat(transaction.hasParts()).isFalse();
    }
}
