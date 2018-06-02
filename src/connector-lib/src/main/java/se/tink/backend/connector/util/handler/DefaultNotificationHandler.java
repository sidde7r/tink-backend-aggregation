package se.tink.backend.connector.util.handler;

import se.tink.backend.core.NotificationSettings;

public class DefaultNotificationHandler implements NotificationHandler {

    @Override
    public NotificationSettings getSettings() {
        NotificationSettings notificationSettings = new NotificationSettings();

        // TODO: which settings should be the standard/general settings?

        notificationSettings.setEinvoices(true);
        notificationSettings.setDoubleCharge(true);
        notificationSettings.setBudget(false);
        notificationSettings.setFraud(false);
        notificationSettings.setBalance(true);
        notificationSettings.setIncome(false);
        notificationSettings.setLargeExpense(true);
        notificationSettings.setSummaryMonthly(false);
        notificationSettings.setTransaction(false);
        notificationSettings.setUnusualAccount(false);
        notificationSettings.setSummaryWeekly(false);
        notificationSettings.setUnusualCategory(false);

        return notificationSettings;
    }
}
