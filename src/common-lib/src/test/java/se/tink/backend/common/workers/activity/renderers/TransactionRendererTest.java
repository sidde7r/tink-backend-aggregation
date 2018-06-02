package se.tink.backend.common.workers.activity.renderers;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;

public class TransactionRendererTest {

    private static ActivityRendererContext context = new ActivityRendererContext();
    private static TransactionRenderer renderer = new TransactionRenderer(context, new DeepLinkBuilderFactory(""));

    @BeforeClass
    public static void beforeClass() {
        context.setCategoryConfiguration(new SECategories());
    }

    @Test
    public void largeExpenseShouldBeCritical() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.LARGE_EXPENSE);
        Assert.assertEquals(Icon.IconColorTypes.CRITICAL, renderer.getIconColor(activity));
    }

    @Test
    public void bankFeeShouldBeCritical() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.BANK_FEE);
        Assert.assertEquals(Icon.IconColorTypes.CRITICAL, renderer.getIconColor(activity));
    }

    @Test
    public void multipleTransactionShouldBeExpenseColor() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.TRANSACTION_MULTIPLE);
        Assert.assertEquals(Icon.IconColorTypes.EXPENSE, renderer.getIconColor(activity));
    }

    @Test
    public void multipleIncomesShouldBeIncomeColor() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.INCOME_MULTIPLE);
        Assert.assertEquals(Icon.IconColorTypes.INCOME, renderer.getIconColor(activity));
    }

    @Test
    public void doubleChargeShouldBeCritical() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.DOUBLE_CHARGE);
        Assert.assertEquals(Icon.IconColorTypes.CRITICAL, renderer.getIconColor(activity));
    }

    @Test
    public void multipleLargeExpenseShouldBeCritical() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.LARGE_EXPENSE_MULTIPLE);
        Assert.assertEquals(Icon.IconColorTypes.CRITICAL, renderer.getIconColor(activity));
    }

    @Test
    public void multipleBankFeesShouldBeCritical() {
        Activity activity = new Activity();
        activity.setType(Activity.Types.BANK_FEE_MULTIPLE);
        Assert.assertEquals(Icon.IconColorTypes.CRITICAL, renderer.getIconColor(activity));
    }

    @Test
    public void incomeCategoryShouldBeIncomeColor() {
        Assert.assertEquals(Icon.IconColorTypes.INCOME,
                renderer.getIconColorType(SECategories.Codes.INCOME_BENEFITS, CategoryTypes.INCOME));
    }

    @Test
    public void expenseCategoryShouldBeExpenseColor() {
        Assert.assertEquals(Icon.IconColorTypes.EXPENSE,
                renderer.getIconColorType(SECategories.Codes.EXPENSES_ENTERTAINMENT, CategoryTypes.EXPENSES));
    }

    @Test
    public void transferCategoryShouldBeTransferColor() {
        Assert.assertEquals(Icon.IconColorTypes.TRANSFER,
                renderer.getIconColorType(SECategories.Codes.TRANSFERS_EXCLUDE, CategoryTypes.TRANSFERS));
    }

    @Test
    public void uncategorizedShouldBeUncategorizedColor() {
        Assert.assertEquals(Icon.IconColorTypes.UNCATEGORIZED,
                renderer.getIconColorType(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED, CategoryTypes.EXPENSES));
    }

}
