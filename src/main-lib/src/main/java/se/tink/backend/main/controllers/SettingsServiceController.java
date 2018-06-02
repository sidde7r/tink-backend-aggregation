package se.tink.backend.main.controllers;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.I18nSettings;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.PeriodSettings;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.notifications.NotificationGroupSettings;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.rpc.UpdateI18nSettingsCommand;
import se.tink.backend.rpc.UpdateNotificationSettingsCommand;
import se.tink.backend.rpc.UpdatePeriodSettingsCommand;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.client.SystemServiceFactory;

public class SettingsServiceController {

    private final FirehoseQueueProducer firehoseQueueProducer;
    private final UserRepository userRepository;
    private final Cluster cluster;
    private final SystemServiceFactory systemServiceFactory;

    @Inject
    public SettingsServiceController(FirehoseQueueProducer firehoseQueueProducer, UserRepository userRepository,
            Cluster cluster, SystemServiceFactory systemServiceFactory) {
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.userRepository = userRepository;
        this.cluster = cluster;
        this.systemServiceFactory = systemServiceFactory;
    }

    public NotificationGroupSettings getNotificationSettings(User user) {
        return NotificationGroupSettings.builder()
                .withCluster(cluster)
                .withLocale(user.getProfile().getLocale())
                .withSettings(user.getProfile().getNotificationSettings())
                .build();
    }

    public NotificationGroupSettings updateNotificationSettings(User user,
            UpdateNotificationSettingsCommand updateNotificationSettingsCommand) {

        updateNotificationSettingsProperties(user.getProfile().getNotificationSettings(), updateNotificationSettingsCommand);

        user = userRepository.save(user);

        return NotificationGroupSettings.builder()
                .withCluster(cluster)
                .withLocale(user.getProfile().getLocale())
                .withSettings(user.getProfile().getNotificationSettings())
                .build();
    }

    public PeriodSettings getPeriodSettings(User user) {
        return user.getProfile().getPeriodSettings();
    }

    public PeriodSettings updatePeriodSettings(User user, UpdatePeriodSettingsCommand updatePeriodSettingsCommand) {
        ProcessService processService = systemServiceFactory.getProcessService();
        updatePeriodSettingsProperties(user.getProfile(), updatePeriodSettingsCommand);

        user = userRepository.save(user);
        processService.generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.FULL);
        firehoseQueueProducer.sendUserConfigurationMessage(user.getId(), FirehoseMessage.Type.UPDATE, user.getFlags(),
                user.getProfile());
        return user.getProfile().getPeriodSettings();
    }

    public I18nSettings updateI18nSettings(User user, UpdateI18nSettingsCommand command) {
        user.getProfile().setLocale(command.getLocaleCode());
        user = userRepository.save(user);
        firehoseQueueProducer.sendUserConfigurationMessage(user.getId(), FirehoseMessage.Type.UPDATE, user.getFlags(),
                user.getProfile());
        return new I18nSettings(user.getProfile().getLocale());
    }

    private void updatePeriodSettingsProperties(UserProfile profile, UpdatePeriodSettingsCommand command) {
        PeriodSettings settings = profile.getPeriodSettings();
        if (isModifiedField(command.getMode(), settings.getMode())) {
            profile.setPeriodMode(command.getMode());
        }

        if (isModifiedField(command.getMonthlyAdjustedDay(), settings.getAdjustedPeriodDay())) {
            profile.setPeriodAdjustedDay(command.getMonthlyAdjustedDay());
        }
    }

    private void updateNotificationSettingsProperties(NotificationSettings notificationSettings, UpdateNotificationSettingsCommand command) {
        if (isModifiedField(command.getBalance(), notificationSettings.getBalance())) {
            notificationSettings.setBalance(command.getBalance());
        }

        if (isModifiedField(command.getBudget(), notificationSettings.getBudget())) {
            notificationSettings.setBudget(command.getBudget());
        }

        if (isModifiedField(command.getDoubleCharge(), notificationSettings.getDoubleCharge())) {
            notificationSettings.setDoubleCharge(command.getDoubleCharge());
        }

        if (isModifiedField(command.getIncome(), notificationSettings.getIncome())) {
            notificationSettings.setIncome(command.getIncome());
        }

        if (isModifiedField(command.getLargeExpense(), notificationSettings.getLargeExpense())) {
            notificationSettings.setLargeExpense(command.getLargeExpense());
        }

        if (isModifiedField(command.getSummaryMonthly(), notificationSettings.getSummaryMonthly())) {
            notificationSettings.setSummaryMonthly(command.getSummaryMonthly());
        }

        if (isModifiedField(command.getSummaryWeekly(), notificationSettings.getSummaryWeekly())) {
            notificationSettings.setSummaryWeekly(command.getSummaryWeekly());
        }

        if (isModifiedField(command.getTransaction(), notificationSettings.getTransaction())) {
            notificationSettings.setTransaction(command.getTransaction());
        }

        if (isModifiedField(command.getUnusualAccount(), notificationSettings.getUnusualAccount())) {
            notificationSettings.setUnusualAccount(command.getUnusualAccount());
        }

        if (isModifiedField(command.getUnusualCategory(), notificationSettings.getUnusualCategory())) {
            notificationSettings.setUnusualCategory(command.getUnusualCategory());
        }

        if (isModifiedField(command.getEinvoices(), notificationSettings.getEinvoices())) {
            notificationSettings.setEinvoices(command.getEinvoices());
        }

        if (isModifiedField(command.getFraud(), notificationSettings.isFraud())) {
            notificationSettings.setFraud(command.getFraud());
        }

        if (isModifiedField(command.getLeftToSpend(), notificationSettings.getLeftToSpend())) {
            notificationSettings.setLeftToSpend(command.getLeftToSpend());
        }

        if (isModifiedField(command.getLoanUpdate(), notificationSettings.getLoanUpdate())) {
            notificationSettings.setLoanUpdate(command.getLoanUpdate());
        }
    }

    private <T> boolean isModifiedField(T newField, T oldField) {
        return newField != null && !Objects.equal(newField, oldField);
    }
}
