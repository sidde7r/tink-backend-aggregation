package se.tink.backend.common.template;

public enum Template {
    ACTIVITIES_BASE_LAYOUT_HTML("activities/base-layout.html"),
    ACTIVITIES_BASE_LEFT_ONLY_LAYOUT_HTML("activities/base-left-only-layout.html"),
    ACTIVITIES_E_INVOICES_PLURAL_COLLAPSED_HTML("activities/e-invoices-plural-collapsed.html"),
    ACTIVITIES_FRAUD_HTML("activities/fraud.html"),
    ACTIVITIES_PERIOD_SUMMARIES_BUDGET_SUMMARY_HTML("activities/period-summaries/budget-summary.html"),
    ACTIVITIES_PERIOD_SUMMARIES_EXPENSES_THIS_WEEK_HTML("activities/period-summaries/expenses-this-week.html"),
    ACTIVITIES_PERIOD_SUMMARIES_LEFT_TO_SPEND_CHART_HTML("activities/period-summaries/left-to-spend-chart.html"),
    ACTIVITIES_PERIOD_SUMMARIES_UNUSUAL_SPENDING_HTML("activities/period-summaries/unusual-spending.html"),
    ACTIVITIES_PERIOD_SUMMARY_HTML("activities/period-summary.html"),
    ACTIVITIES_SUGGEST_PROVIDER_HTML("activities/suggest-provider.html"),
    ACTIVITIES_TRANSACTION_PLURAL_COLLAPSED_HTML("activities/transaction-plural-collapsed.html"),
    ACTIVITIES_TRANSACTION_SINGLE_HTML("activities/transaction-single.html"),
    ACTIVITIES_YEAR_IN_NUMBERS_HTML("activities/year-in-numbers.html"),
    ACTIVITIES_SUMMER_IN_NUMBERS_HTML("activities/summer-in-numbers.html"),
    ACTIVITIES_ABNAMRO_MONTHLY_SUMMARY_HTML("activities/abnamro-monthly-summary.html"),
    ACTIVITIES_APPLICATION_RESUME_HTML("activities/application-resume.html"),
    ACTIVITIES_LOAN_EVENT_HTML("activities/loan-event-collapsed.html"),
    ACTIVITIES_BADGE_HTML("activities/badge.html"),
    ACTIVITIES_DISCOVER_HTML("activities/discover.html"),
    ACTIVITIES_E_INVOICES_SINGLE_HTML("activities/e-invoices-single.html"),
    ACTIVITIES_EMPTY_HTML("activities/empty.html"),
    ACTIVITIES_LOOKBACK_HTML("activities/lookback.html"),
    ACTIVITIES_MAINTENANCE_HTML("activities/maintenance.html"),
    ACTIVITIES_MERCHANT_MAP_HTML("activities/merchant-map.html"),
    ACTIVITIES_OPEN_SAVINGS_ACCOUNT_HTML("activities/open-savings-account.html"),
    ACTIVITIES_SUGGEST_MERCHANT_HTML("activities/suggest-merchant.html"),
    ACTIVITIES_SWITCH_MORTGAGE_PROVIDER_HTML("activities/switch-mortgage-provider.html"),
    ACTIVITIES_TRANSFER_HTML("activities/transfer.html"),
    ACTIVITIES_BANK_FEE_SELFIE_HTML("activities/bank-fee-selfie.html"),
    MONTHLY_SUMMARY_TEMPLATE_INLINED_HTML("monthly-summary/template.inlined.html"),
    MONTHLY_SUMMARY_TEMPLATE_NO_DATA_INLINED_HTML("monthly-summary/template-no-data.inlined.html"),
    FRAUD_FRAUD_EXTENDED_INFO_HTML("fraud/fraud-extended-info.html"),
    ACTIVITIES_REIMBURSEMENT_SWISH_SINGLE_HTML("activities/reimbursement-swish.html"),
    ACTIVITIES_BUDGET_LAYOUT_HTML("activities/budget-layout.html"),
    ACTIVITIES_RATE_THIS_APP_HTML("activities/rate-this.app.html"),
    ACTIVITIES_BALANCE_HTML("activities/balance.html"),

    ACTIVITIES_BASE_LAYOUT_HTML_V2("activities/v2/base-layout.html"),
    ACTIVITIES_BASE_LEFT_ONLY_LAYOUT_HTML_V2("activities/v2/base-left-only-layout.html"),
    ACTIVITIES_E_INVOICES_PLURAL_COLLAPSED_HTML_V2("activities/v2/e-invoices-plural-collapsed.html"),
    ACTIVITIES_FRAUD_HTML_V2("activities/v2/fraud.html"),
    ACTIVITIES_PERIOD_SUMMARIES_BUDGET_SUMMARY_HTML_V2("activities/v2/period-summaries/budget-summary.html"),
    ACTIVITIES_PERIOD_SUMMARIES_EXPENSES_THIS_WEEK_HTML_V2("activities/v2/period-summaries/expenses-this-week.html"),
    ACTIVITIES_PERIOD_SUMMARIES_LEFT_TO_SPEND_CHART_HTML_V2("activities/v2/period-summaries/left-to-spend-chart.html"),
    ACTIVITIES_PERIOD_SUMMARIES_UNUSUAL_SPENDING_HTML_V2("activities/v2/period-summaries/unusual-spending.html"),
    ACTIVITIES_PERIOD_SUMMARY_HTML_V2("activities/v2/period-summary.html"),
    ACTIVITIES_SUGGEST_PROVIDER_HTML_V2("activities/v2/suggest-provider.html"),
    ACTIVITIES_TRANSACTION_PLURAL_COLLAPSED_HTML_V2("activities/v2/transaction-plural-collapsed.html"),
    ACTIVITIES_TRANSACTION_SINGLE_HTML_V2("activities/v2/transaction-single.html"),
    ACTIVITIES_ABNAMRO_MONTHLY_SUMMARY_HTML_V2("activities/v2/abnamro-monthly-summary.html"),
    ACTIVITIES_APPLICATION_RESUME_HTML_V2("activities/v2/application-resume.html"),
    ACTIVITIES_DISCOVER_HTML_V2("activities/v2/discover.html"),
    ACTIVITIES_E_INVOICES_SINGLE_HTML_V2("activities/v2/e-invoices-single.html"),
    ACTIVITIES_EMPTY_HTML_V2("activities/v2/empty.html"),
    ACTIVITIES_MAINTENANCE_HTML_V2("activities/v2/maintenance.html"),
    ACTIVITIES_OPEN_SAVINGS_ACCOUNT_HTML_V2("activities/v2/open-savings-account.html"),
    ACTIVITIES_SWITCH_MORTGAGE_PROVIDER_HTML_V2("activities/v2/switch-mortgage-provider.html"),
    ACTIVITIES_TRANSFER_HTML_V2("activities/v2/transfer.html"),
    MONTHLY_SUMMARY_TEMPLATE_INLINED_HTML_V2("monthly-summary-v2/template.inlined.html"),
    MONTHLY_SUMMARY_TEMPLATE_NO_DATA_INLINED_HTML_V2("monthly-summary-v2/template-no-data.inlined.html"),
    FRAUD_FRAUD_EXTENDED_INFO_HTML_V2("fraud/v2/fraud-extended-info.html"),
    ACTIVITIES_REIMBURSEMENT_SWISH_SINGLE_HTML_V2("activities/v2/reimbursement-swish.html"),
    ACTIVITIES_BUDGET_LAYOUT_HTML_V2("activities/v2/budget-layout.html"),
    ACTIVITIES_RATE_THIS_APP_HTML_V2("activities/v2/rate-this.app.html"),
    ACTIVITIES_BALANCE_HTML_V2("activities/v2/balance.html");


    private final String filePath;

    Template(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
