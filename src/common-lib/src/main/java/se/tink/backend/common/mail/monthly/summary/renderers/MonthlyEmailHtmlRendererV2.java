package se.tink.backend.common.mail.monthly.summary.renderers;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.mail.monthly.summary.model.BudgetData;
import se.tink.backend.common.mail.monthly.summary.model.EmailContent;
import se.tink.backend.common.mail.monthly.summary.utils.IconsUtil;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.common.template.Template;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;

public class MonthlyEmailHtmlRendererV2 {

    public String renderEmail(PooledRythmProxy pooledRythmProxy, EmailContent input) {

        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(input.getCategoryData());
        Preconditions.checkNotNull(input.getActivityData());

        final Catalog catalog = Catalog.getCatalog(input.getLocale());

        Map<String, Object> context = new HashMap<>();

        addHeadings(context, input, catalog);
        addCategories(context, input, catalog);
        addActivities(context, input, catalog);
        addBudgets(context, input, catalog);
        addFraud(context, input, catalog);
        addMisc(context, input, catalog);
        addFooter(context, input, catalog);

        context.put("cdn", "https://cdn.tink.se/email-assets/monthly-summary");

        return pooledRythmProxy.render(Template.MONTHLY_SUMMARY_TEMPLATE_INLINED_HTML_V2, context);
    }

    private void addCategories(Map<String, Object> context, EmailContent input, Catalog catalog) {
        context.put("categories_title", catalog.getString("Largest expenses"));
        context.put("categories_sub_title", catalog.getString("compared with previous month"));

        context.put("categories", input.getCategoryData());
        context.put("categories_icon_util", new IconsUtil());
        context.put("categories_caption_percent_units", catalog.getString("Percentage points"));
    }

    private void addActivities(Map<String, Object> context, EmailContent input, Catalog catalog) {
        context.put("activities_title", catalog.getString("Important Events"));

        context.put("activities_low_balance_count", input.getActivityData().getLowBalanceCount());
        context.put("activities_large_expense_count", input.getActivityData().getLargeExpenseCount());
        context.put("activities_more_than_usual_count", input.getActivityData().getMoreThanUsualCount());

        context.put("activities_low_balance_caption", catalog.getString("Low balance"));
        context.put("activities_large_expense_caption", catalog.getString("Large expenses"));
        context.put("activities_more_than_usual_caption", catalog.getString("More than usual"));

    }

    private void addBudgets(Map<String, Object> context, EmailContent input, Catalog catalog) {
        context.put("budgets", input.getBudgetData());
        context.put("budgets_title", catalog.getString("Budgets"));
        context.put("budgets_sub_title", getBudgetString(input.getBudgetData(), catalog));
    }

    private void addFraud(Map<String, Object> context, EmailContent input, Catalog catalog) {

        context.put("fraud_enabled_on_user_market", input.getFraudData().isFraudEnabledOnUserMarket());

        Integer fraudUnHandledEventsCount = input.getFraudData().getUnhandledEventsCount();

        context.put("fraud_activated", input.getFraudData().isFraudActive());
        context.put("fraud_unhandled_count", fraudUnHandledEventsCount);
        context.put("fraud_handled_count", input.getFraudData().getUpdatedCount());

        context.put("fraud_caption_name", catalog.getString("ID Control"));

        context.put("fraud_caption_activate_description",
                catalog.getString("Activate ID Control to safeguard your identity and bank accounts"));

        context.put("fraud_caption_handle_warnings", catalog.getString("Handle warnings"));
        context.put("fraud_caption_activate", catalog.getString("Activate ID Control"));
        context.put("fraud_caption_right_now", catalog.getString("You have"));
        context.put("fraud_caption_you_handled", catalog.getString("You handled"));
        context.put("fraud_caption_nothing_to_handle", catalog.getString("No warnings to handle"));
        context.put("fraud_caption_event_to_handle", String.format(catalog.getPluralString("%s warning to handle",
                "%s warnings to handle", fraudUnHandledEventsCount), fraudUnHandledEventsCount));

        context.put("fraud_caption_event", catalog.getPluralString("Event latest month", "Events latest month",
                input.getFraudData().getUpdatedCount()));
    }

    private void addMisc(Map<String, Object> context, EmailContent input, Catalog catalog) {
        context.put("userId", input.getUserId());
        context.put("openButtonCaption", catalog.getString("Open Tink"));

    }

    private void addHeadings(Map<String, Object> context, EmailContent input, Catalog catalog) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(catalog.getString("d MMMMM"),
                Catalog.getLocale(input.getLocale()));

        context.put("title_main", input.getTitle());

        context.put("title_date_description", Catalog.format(catalog.getString("{0} to {1}"),
                dateFormat.format(input.getStartDate()), dateFormat.format(DateUtils.addDays(input.getEndDate(), 1))));

        int categorizedTransactions = input.getNumberOfCategorizedTransactions();

        String message;

        if (input.getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED)) {
            message = catalog.getPluralString(
                    "Tink has automatically categorized {0} transaction the past salary period.",
                    "Tink has automatically categorized {0} transactions the past salary period.",
                    categorizedTransactions);
        } else {
            message = catalog.getPluralString("Tink has automatically categorized {0} transaction the past month.",
                    "Tink has automatically categorized {0} transactions the past month.", categorizedTransactions);
        }

        context.put("title_categorization_count", Catalog.format(message, categorizedTransactions));
    }

    private void addFooter(Map<String, Object> context, EmailContent input, Catalog catalog) {
        String link = String.format("https://app.tink.se/subscriptions/%s?locale=%s",
                input.getUnsubscribeToken(), Catalog.getLocale(input.getLocale()).getLanguage());

        String linkFormat = catalog.getString(
                "If you no longer want to receive these emails, please click <a href=\"%s\">here</a>.");

        context.put("footer", String.format(linkFormat, link));
    }

    private String getBudgetString(List<BudgetData> budgetData, Catalog catalog) {
        int numberOfBudgets = budgetData.size();

        if (budgetData.size() > 0) {

            int numberOfPassed = FluentIterable.from(budgetData).filter(b -> (b.getPercentCompleted() < 100)).size();

            double ratioOfPassedBudgets = (double) numberOfPassed / (double) numberOfBudgets;

            if (numberOfPassed == numberOfBudgets) {
                return catalog.getPluralString(
                        "You kept your goal. Great job!", "You kept all your goals. Great job!",
                        numberOfBudgets);
            } else if (numberOfPassed == 0) {
                return (catalog.getPluralString(
                        "You did not keep your goal. Better luck next month.",
                        "You did not keep a single goal. Better luck next month.", numberOfBudgets));
            } else if (numberOfPassed > 0) {
                String budgetDescriptiveFeedback = Catalog.format(
                        catalog.getString("You kept {0} of your goals."),
                        numberOfPassed, numberOfBudgets);

                String description = "Come on!";

                if (ratioOfPassedBudgets > 0.80) {
                    description = "Great job!";
                } else if (ratioOfPassedBudgets > 0.5) {
                    description = "Good job!";
                }

                return String.format("%s %s", budgetDescriptiveFeedback, catalog.getString(description));
            }

        }

        return null;
    }

}


