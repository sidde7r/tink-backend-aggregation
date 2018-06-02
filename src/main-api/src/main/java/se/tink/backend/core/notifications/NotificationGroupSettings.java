package se.tink.backend.core.notifications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.core.NotificationSettings;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class NotificationGroupSettings {
    private static final LocalizableKey INCOME = new LocalizableKey("Incomes");
    private static final LocalizableKey EXPENSES = new LocalizableKey("Expenses");
    private static final LocalizableKey LEFT_TO_SPEND = new LocalizableKey("Left to spend");
    private static final LocalizableKey LOW_BALANCE = new LocalizableKey("Low balance");
    private static final LocalizableKey DOUBLE_CHARGE = new LocalizableKey("Double charge");
    private static final LocalizableKey LARGE_EXPENSES = new LocalizableKey("Large expenses");
    private static final LocalizableKey MORE_THAN_USUAL = new LocalizableKey("More than usual");
    private static final LocalizableKey UNUSUAL_ACCOUNT_ACTIVITY = new LocalizableKey("Unusual account activity");
    private static final LocalizableKey ID_CONTROL_EVENTS = new LocalizableKey("ID Control event");
    private static final LocalizableKey WEEKLY = new LocalizableKey("Weekly");
    private static final LocalizableKey MONTHLY = new LocalizableKey("Monthly");
    private static final LocalizableKey E_INVOICES = new LocalizableKey("E-invoices");
    private static final LocalizableKey EXPENSES_OVER_GOAL = new LocalizableKey("Expenses over goal");

    private static final LocalizableKey INCOME_AND_EXPENSES = new LocalizableKey("Income & Expenses");
    private static final LocalizableKey IRREGULARITIES = new LocalizableKey("Irregularities");
    private static final LocalizableKey SUMMARIES = new LocalizableKey("Summaries");
    private static final LocalizableKey BUDGETS = new LocalizableKey("Budgets");

    public static final class Type {
        public static final String BALANCE = "balance";
        public static final String DOUBLE_CHARGE = "double_charge";
        public static final String INCOME = "income";
        public static final String LARGE_EXPENSE = "large_expense";
        public static final String BUDGET = "budget";
        public static final String UNUSUAL_ACCOUNT = "unusual_account";
        public static final String UNUSUAL_CATEGORY = "unusual_category";
        public static final String TRANSACTION = "transaction";
        public static final String SUMMARY_WEEKLY = "summary_weekly";
        public static final String SUMMARY_MONTHLY = "summary_monthly";
        public static final String E_INVOICES = "e_invoices";
        public static final String FRAUD = "fraud";
        public static final String LEFT_TO_SPEND = "left_to_spend";
    }

    private List<NotificationGroup> groups;

    public List<NotificationGroup> getGroups() {
        return groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private Cluster cluster;
        private String locale;
        private NotificationSettings settings;

        public Builder withCluster(Cluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder withLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder withSettings(NotificationSettings settings) {
            this.settings = settings;
            return this;
        }

        public NotificationGroupSettings build() {
            Catalog catalog = Catalog.getCatalog(locale);

            NotificationType income = new NotificationType(Type.INCOME,
                    catalog.getString(INCOME), settings.getIncome());

            NotificationType expenses = new NotificationType(Type.TRANSACTION,
                    catalog.getString(EXPENSES), settings.getTransaction());

            NotificationType leftToSpend = new NotificationType(Type.LEFT_TO_SPEND,
                    catalog.getString(LEFT_TO_SPEND), settings.getLeftToSpend());

            NotificationType lowBalance = new NotificationType(Type.BALANCE,
                    catalog.getString(LOW_BALANCE), settings.getBalance());

            NotificationType doubleCharge = new NotificationType(Type.DOUBLE_CHARGE,
                    catalog.getString(DOUBLE_CHARGE), settings.getDoubleCharge());

            NotificationType largeExpenses = new NotificationType(Type.LARGE_EXPENSE,
                    catalog.getString(LARGE_EXPENSES), settings.getLargeExpense());

            NotificationType moreThanUsual = new NotificationType(Type.UNUSUAL_CATEGORY,
                    catalog.getString(MORE_THAN_USUAL), settings.getUnusualCategory());

            NotificationType unusualAccountActivity = new NotificationType(Type.UNUSUAL_ACCOUNT,
                    catalog.getString(UNUSUAL_ACCOUNT_ACTIVITY), settings.getUnusualAccount());

            NotificationType idControlEvents = new NotificationType(Type.FRAUD,
                    catalog.getString(ID_CONTROL_EVENTS), settings.isFraud());

            NotificationType weeklySummary = new NotificationType(Type.SUMMARY_WEEKLY,
                    catalog.getString(WEEKLY), settings.getSummaryWeekly());

            NotificationType monthlySummary = new NotificationType(Type.SUMMARY_MONTHLY,
                    catalog.getString(MONTHLY), settings.getSummaryMonthly());

            NotificationType eInvoices = new NotificationType(Type.E_INVOICES,
                    catalog.getString(E_INVOICES), settings.getEinvoices());

            NotificationType expensesOverGoal = new NotificationType(Type.BUDGET,
                    catalog.getString(EXPENSES_OVER_GOAL), settings.getBudget());

            NotificationGroup incomeAndExpenses = new NotificationGroup(catalog.getString(INCOME_AND_EXPENSES));
            incomeAndExpenses.addNotificationType(income);
            incomeAndExpenses.addNotificationType(expenses);
            incomeAndExpenses.addNotificationType(leftToSpend);

            NotificationGroup irregularities = new NotificationGroup(catalog.getString(IRREGULARITIES));
            irregularities.addNotificationType(lowBalance);
            irregularities.addNotificationType(doubleCharge);
            irregularities.addNotificationType(largeExpenses);
            irregularities.addNotificationType(moreThanUsual);
            irregularities.addNotificationType(unusualAccountActivity);

            NotificationGroup summaries = new NotificationGroup(catalog.getString(SUMMARIES));
            summaries.addNotificationType(weeklySummary);
            summaries.addNotificationType(monthlySummary);

            NotificationGroup budgets = new NotificationGroup(catalog.getString(BUDGETS));
            budgets.addNotificationType(expensesOverGoal);

            if (cluster != Cluster.ABNAMRO) {
                incomeAndExpenses.addNotificationType(eInvoices);
                irregularities.addNotificationType(idControlEvents);
            }

            NotificationGroupSettings result = new NotificationGroupSettings();
            result.groups = ImmutableList.of(incomeAndExpenses, irregularities, summaries, budgets);
            return result;
        }
    }
}
