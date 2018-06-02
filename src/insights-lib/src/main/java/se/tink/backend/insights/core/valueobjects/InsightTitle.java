package se.tink.backend.insights.core.valueobjects;

public enum InsightTitle {
    ACCOUNT_BALANCE_LOW("Your balance on %s is low"),
    BUDGET_CLOSE("You are close to go over your budget for %s"),
    BUDGET_OVERSPEND("You have overspent on your budget for %s"),
    EINVOICE("E-Invoice to approve"),
    EINVOICE_OVERDUE("Overdue E-Invoice"),
    GENERIC_FRAUD("%s"),
    HIGHER_INCOME_THAN_CERTAIN_PERCENTILE("Your income is higher than %d%% of all Tink users!"),
    INCREASE_CATEGORIZATION_LEVEL("Improve you categorization and Tink will work better for you!"),
    LEFT_TO_SPEND_HIGH("You have %d kr left of your salary!"),
    LEFT_TO_SPEND_LOW("You don't have any money from your salary left this month."),
    MONTHLY_SUMMARY("Last month in numbers"),
    RATE_THIS_APP("Do you like Tink?"),
    RESIDENCE_DO_YOU_OWN_IT("It seems like you're registered at %s"),
    WEEKLY_SUMMARY("Last week in numbers"),
    ALL_BANKS_CONNECTED("Do you have all of your banks and services connected to Tink?");

    private final String value;

    InsightTitle(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
