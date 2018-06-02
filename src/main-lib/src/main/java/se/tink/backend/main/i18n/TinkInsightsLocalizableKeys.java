package se.tink.backend.main.i18n;

import se.tink.libraries.i18n.LocalizableKey;

public class TinkInsightsLocalizableKeys implements InsightsLocalizableKeys {
    private static final LocalizableKey MORTGAGE_TITLE_LOW_RATE = new LocalizableKey(
            "Your mortgage rate is lower than most others.");
    private static final LocalizableKey MORTGAGE_TITLE_MEDIUM_RATE = new LocalizableKey(
            "Your mortgage rate is decent, but can be better.");
    private static final LocalizableKey MORTGAGE_TITLE_HIGH_RATE = new LocalizableKey(
            "Your mortgage rate is quite high. Maybe we can help?");
    private static final LocalizableKey MORTGAGE_BODY = new LocalizableKey(
            "In Tink you get help to keep track on your interest rate and the value of the residence. You can also lower your interest rate directly in the app!");
    private static final LocalizableKey CATEGORIES_TITLE = new LocalizableKey(
            "You spend the most money on this the last month.");
    private static final LocalizableKey CATEGORIES_BODY = new LocalizableKey(
            "In Tink you get on top of where you spend your money. Do you need to reduce any expenses? or spend more on something that’s important to you?");
    private static final LocalizableKey DAILY_SPENDING_TITLE = new LocalizableKey("You spend on average {0} per day.");
    private static final LocalizableKey DAILY_SPENDING_BODY = new LocalizableKey(
            "The last few months, you’re spending the most on {0}, on which you spend on average {1}.");
    private static final LocalizableKey LEFT_TO_SPEND_TITLE_HAVE_MONEY = new LocalizableKey(
            "You normally have a bunch of money left of your salary.");
    private static final LocalizableKey LEFT_TO_SPEND_BODY_HAVE_MONEY = new LocalizableKey(
            "Deposit the surplus every month into a savings account where it grows over time. Open a savings account with interest rate directly in Tink.");
    private static final LocalizableKey SAVINGS_TITLE_MORE_THAN_AVERAGE_WITH_INVESTMENTS = new LocalizableKey(
            "You have a very good amount saved. You belong to the top {0}!");
    private static final LocalizableKey SAVINGS_TITLE_MORE_THAN_AVERAGE = new LocalizableKey(
            "You have a good savings buffer! More than {0} over average.");
    private static final LocalizableKey SAVINGS_TITLE_LESS_THAN_AVERAGE = new LocalizableKey(
            "Your savings buffer is slightly lower than average. We can help!");
    private static final LocalizableKey SAVINGS_BODY = new LocalizableKey(
            "Tink helps you keep track of all you accounts. You can create a new account with a good savings rate and setup savings goals to help you get started!");

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
