package se.tink.backend.grpc.v1.converter.settings;

import se.tink.backend.core.notifications.NotificationGroupSettings;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.UpdateNotificationSettingsCommand;
import se.tink.grpc.v1.models.NotificationType;
import se.tink.grpc.v1.rpc.UpdateNotificationSettingsRequest;

public class NotificationSettingsRequestConverter implements
        Converter<UpdateNotificationSettingsRequest, UpdateNotificationSettingsCommand> {

    @Override
    public UpdateNotificationSettingsCommand convertFrom(UpdateNotificationSettingsRequest input) {
        UpdateNotificationSettingsCommand command = new UpdateNotificationSettingsCommand();

        for (NotificationType notificationType : input.getNotificationTypesList()) {

            switch (notificationType.getKey()) {
            case NotificationGroupSettings.Type.BALANCE:
                command.setBalance(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.BUDGET:
                command.setBudget(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.DOUBLE_CHARGE:
                command.setDoubleCharge(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.E_INVOICES:
                command.setEinvoices(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.FRAUD:
                command.setFraud(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.INCOME:
                command.setIncome(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.LARGE_EXPENSE:
                command.setLargeExpense(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.LEFT_TO_SPEND:
                command.setLeftToSpend(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.SUMMARY_MONTHLY:
                command.setSummaryMonthly(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.SUMMARY_WEEKLY:
                command.setSummaryWeekly(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.TRANSACTION:
                command.setTransaction(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.UNUSUAL_ACCOUNT:
                command.setUnusualAccount(notificationType.getEnabled());
                break;
            case NotificationGroupSettings.Type.UNUSUAL_CATEGORY:
                command.setUnusualCategory(notificationType.getEnabled());
                break;
            }
        }

        return command;
    }
}
