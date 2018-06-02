package se.tink.backend.main.i18n;

import se.tink.libraries.i18n.LocalizableKey;

public class AbnAmroInsightsLocalizableKeys implements InsightsLocalizableKeys {

    // Copy from Tink
    private static final LocalizableKey MORTGAGE_TITLE_LOW_RATE = new LocalizableKey(
            "Your mortgage interest rate is lower than most others.");
    private static final LocalizableKey MORTGAGE_TITLE_MEDIUM_RATE = new LocalizableKey(
            "Your mortgage interest rate is decent, but there's room for improvement.");
    private static final LocalizableKey MORTGAGE_TITLE_HIGH_RATE = new LocalizableKey(
            "Your mortgage interest rate is quite high. Maybe we can help?");
    private static final LocalizableKey MORTGAGE_BODY = new LocalizableKey(
            "In Grip you get help to keep track on your interest rate and the value of the residence. You can also lower your interest rate directly in the app!");
    private static final LocalizableKey SAVINGS_TITLE_MORE_THAN_AVERAGE_WITH_INVESTMENTS = new LocalizableKey(
            "Congratulation! You have a good amount saved. You belong to the top {0}!");
    private static final LocalizableKey SAVINGS_TITLE_MORE_THAN_AVERAGE = new LocalizableKey(
            "You have a good savings buffer! More than {0} over average. Keep up the good job!");
    private static final LocalizableKey SAVINGS_TITLE_LESS_THAN_AVERAGE = new LocalizableKey(
            "Your savings buffer is a little bit lower than average. Let us help you improve!");
    private static final LocalizableKey SAVINGS_BODY = new LocalizableKey(
            "Grip helps you keep track of all you accounts. You can create a new account with a good savings rate and setup savings goals to help you get started!");

    // Special copy for ABN AMRO
    private static final LocalizableKey CATEGORIES_TITLE = new LocalizableKey(
            "The last month you have spent the most money on this.");
    private static final LocalizableKey CATEGORIES_BODY = new LocalizableKey(
            "Would you like to spend less or more in these categories? By settings budgets you get more insights and control.");
    private static final LocalizableKey DAILY_SPENDING_TITLE = new LocalizableKey("You spend on average {0} per day.");
    private static final LocalizableKey DAILY_SPENDING_BODY = new LocalizableKey(
            "The last months you have spend the most on {0}: {1}.");
    private static final LocalizableKey LEFT_TO_SPEND_TITLE_HAVE_MONEY = new LocalizableKey(
            "You have each month a nice amount left.");
    private static final LocalizableKey LEFT_TO_SPEND_BODY_HAVE_MONEY = new LocalizableKey(
            "Maybe it is a good idea to transfer this amount to a savings account. In this way you save easily money for vacation or unexpected expenses.");

    @Override
    public LocalizableKey getMortgageTitleLowRate() {
        return MORTGAGE_TITLE_LOW_RATE;
    }

    @Override
    public LocalizableKey getMortgageTitleMediumRate() {
        return MORTGAGE_TITLE_MEDIUM_RATE;
    }

    @Override
    public LocalizableKey getMortgageTitleHighRate() {
        return MORTGAGE_TITLE_HIGH_RATE;
    }

    @Override
    public LocalizableKey getMortgageBody() {
        return MORTGAGE_BODY;
    }

    @Override
    public LocalizableKey getCategoriesTitle() {
        return CATEGORIES_TITLE;
    }

    @Override
    public LocalizableKey getCategoriesBody() {
        return CATEGORIES_BODY;
    }

    @Override
    public LocalizableKey getDailySpendingTitle() {
        return DAILY_SPENDING_TITLE;
    }

    @Override
    public LocalizableKey getDailySpendingBody() {
        return DAILY_SPENDING_BODY;
    }

    @Override
    public LocalizableKey getLeftToSpendTitleHaveMoney() {
        return LEFT_TO_SPEND_TITLE_HAVE_MONEY;
    }

    @Override
    public LocalizableKey getLeftToSpendBodyHaveMoney() {
        return LEFT_TO_SPEND_BODY_HAVE_MONEY;
    }

    @Override
    public LocalizableKey getSavingsTitleMoreThanAverageWithInvestments() {
        return SAVINGS_TITLE_MORE_THAN_AVERAGE_WITH_INVESTMENTS;
    }

    @Override
    public LocalizableKey getSavingsTitleMoreThanAverage() {
        return SAVINGS_TITLE_MORE_THAN_AVERAGE;
    }

    @Override
    public LocalizableKey getSavingsTitleLessThanAverage() {
        return SAVINGS_TITLE_LESS_THAN_AVERAGE;
    }

    @Override
    public LocalizableKey getSavingsBody() {
        return SAVINGS_BODY;
    }
}
