package se.tink.backend.system.cronjob;

import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.atomic.AtomicBoolean;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.scheduler.SchedulerFactory;
import se.tink.backend.rpc.RefreshCredentialSchedulationRequest;
import se.tink.backend.system.LeaderCandidate;
import se.tink.backend.system.rpc.SendMonthlyEmailsRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.Gauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import static org.quartz.CronScheduleBuilder.cronSchedule;

public class CronJobManager implements Managed {
    private static final LogUtils log = new LogUtils(CronJobManager.class);
    private final MetricId CURRENTLY_EXECUTING_JOBS = MetricId.newId("cron_jobs_executing");

    private static LeaderCandidate leaderCandidate;
    private ServiceConfiguration config;
    private Scheduler scheduler;
    private MetricRegistry metricRegistry;

    public CronJobManager(ServiceContext serviceContext, LeaderCandidate leaderCandidate,
            MetricRegistry metricRegistry) {

        this.config = serviceContext.getConfiguration();
        this.metricRegistry = metricRegistry;

        CronJobManager.leaderCandidate = leaderCandidate;

        // Globally make sure all cron jobs have access to system service.
        SystemCronJob.setSystemServiceFactory(serviceContext.getSystemServiceFactory());
        SystemCronJob.setMetricRegistry(metricRegistry);
    }

    public static AtomicBoolean isCurrentInstanceLeader() {
        return leaderCandidate.isCurrentInstanceLeader();
    }

    @Override
    public void start() throws Exception {
        if (config.getSchedulerConfiguration() != null && config.getSchedulerConfiguration().isEnabled()) {
            scheduler = new SchedulerFactory(config.getSchedulerConfiguration()).build();

            metricRegistry.registerSingleton(CURRENTLY_EXECUTING_JOBS, new Gauge() {
                @Override
                public double getValue() {
                    try {
                        return scheduler.getCurrentlyExecutingJobs().size();
                    } catch (SchedulerException e) {
                        log.error("Could not collect the number of executing jobs.", e);
                        return 0;
                    }
                }
            });

            // See http://bit.ly/1wtuCZv for documentation on the cron format (which cronSchedule method JavaDoc lacks).

            // Please consider cronjob overlap when adding them here. Multiple cronjobs might result in out of memory
            // error.

            if (config.getCluster() == Cluster.TINK) {
                scheduleAutomaticRefreshJob();
                if (config.isFraudCronEnabled()) {
                    scheduleSendFraudReminderJob();
                }
                scheduleReportStatisticsJob();
                schedulePermanentErrorMarkerJob();
                scheduleAutomaticRefreshFailedCredentialsJob();
                scheduleSendNotificationsJob();
                scheduleUpdateFacebookProfileJob();
                scheduleSendMessageForFailingCredentialsJob();
                scheduleSendManualRefreshReminderJob();
                scheduleResetHangingCredentialsJob();
                scheduleSendUserActivationReminderJob();
                scheduleRefreshApplicationsJob();
                scheduleApplicationsReportingJob();
                scheduleRefreshChangedFraudCredentialsJob();
                scheduleSendPaydayReminderJob();
                scheduleSendMonthlySummaryEmailJob();
                scheduleCleanUpOAuth2ClientsUsers();
                scheduleRefreshProductsJob();
                scheduleDetectTransactionIndexDivergence();
                scheduleAuthenticationCleanup();
            } else if (config.getCluster() == Cluster.ABNAMRO) {
                scheduleAuthenticationCleanup();
                scheduleAutomaticRefreshJob();
                scheduleReportStatisticsJob();
                scheduleAutomaticRefreshFailedCredentialsJob();
                scheduleSendNotificationsJob();
                scheduleResetHangingCredentialsJob();
                scheduleSendFallbackNotificationJob();
            } else if (config.getCluster() == Cluster.CORNWALL) {
                scheduleSendNotificationsJob();
            }

            // Jobs that runs on all clusters
            scheduleDeletePartiallyDeletedUsers();
        }
    }

    private void scheduleSendFallbackNotificationJob() {
        // Every 1 minutes.
        scheduleJob(SendFallbackNotificationJob.class, cronSchedule("0 0/1 * * * ?"));
    }

    private void scheduleSendMonthlySummaryEmailJob() {
        // Once per day, at 09:00 in the morning
        scheduleJob(SendMonthlySummaryEmailJob.class, cronSchedule("0 0 9 * * ?"));
    }

    private void scheduleSendPaydayReminderJob() {
        // Once per day, at 15:55 in the afternoon.
        scheduleJob(SendPaydayReminderJob.class, cronSchedule("0 55 15 * * ?"));
    }

    private void scheduleRefreshChangedFraudCredentialsJob() {
        // Once per day at 09:07 in the morning.
        scheduleJob(RefreshChangedFraudCredentialsJob.class, cronSchedule("0 7 9 * * ?"));
    }

    private void scheduleSendUserActivationReminderJob() {
        // Once per day, at 17:00.
        scheduleJob(SendUserActivationReminderJob.class, cronSchedule("0 0 17 * * ?"));
    }

    private void scheduleResetHangingCredentialsJob() {
        // Every 15 minutes when minute part is 0, 15 or 45 and second part is 55.
        scheduleJob(ResetHangingCredentialsJob.class, cronSchedule("55 0,15,45 * * * ?"));
    }

    private void scheduleSendManualRefreshReminderJob() {
        // Once per day, at 16:00 in the afternoon
        scheduleJob(SendManualRefreshReminderJob.class, cronSchedule("0 0 16 * * ?"));
    }

    private void scheduleSendMessageForFailingCredentialsJob() {
        // At noon and at 19:00 in the evening.
        scheduleJob(SendMessageForFailingCredentialsJob.class, cronSchedule("0 0 12,19 * * ?"));
    }

    private void scheduleUpdateFacebookProfileJob() {
        // Thirty-seven minutes past every whole hour.
        scheduleJob(UpdateFacebookProfileJob.class, cronSchedule("0 37 * * * ?"));
    }

    private void scheduleSendNotificationsJob() {
        // every minute
        scheduleJob(SendNotificationsJob.class, cronSchedule("0 * * * * ?"));
    }

    private void scheduleAutomaticRefreshFailedCredentialsJob() {
        // Three minutes past 15 o'clock in the afternoon.
        scheduleJob(AutomaticRefreshFailedCredentialsJob.class, cronSchedule("0 3 15 * * ?"));
    }

    private void schedulePermanentErrorMarkerJob() {
        // Twenty minutes past midnight.
        scheduleJob(PermanentErrorMarkerJob.class, cronSchedule("0 20 0 * * ?"));
    }

    private void scheduleReportStatisticsJob() {
        // Midnight
        scheduleJob(ReportStatisticsJob.class, cronSchedule("0 0 0 * * ?"));
    }

    private void scheduleSendFraudReminderJob() {
        // Once every hour when the minute part is 25.
        scheduleJob(SendFraudReminderJob.class, cronSchedule("0 25 * * * ?"));
    }

    private void scheduleAutomaticRefreshJob() {
        // Every 5 minutes.
        scheduleJob(AutomaticRefreshJob.class, cronSchedule("0 0/5 * * * ?"));
    }

    private void scheduleCleanUpOAuth2ClientsUsers() {
        // The 51st minute every 4 hours.
        scheduleJob(CleanUpOAuth2ClientsUsers.class, cronSchedule("0 51 0/4 * * ?"));
    }

    private void scheduleRefreshApplicationsJob() {
        // Refresh applications every third hour 07:00 to 22:00 (6 times a day).
        scheduleJob(RefreshApplicationsJob.class, cronSchedule("0 0 7,10,13,16,19,22 * * ?"));
    }

    private void scheduleApplicationsReportingJob() {
        // Report applications at 07:00 on the first day of each month.
        scheduleJob(ApplicationsReportingJob.class, cronSchedule("0 0 7 1 * ?"));
    }

    private void scheduleRefreshProductsJob() {
        // Midnight
        scheduleJob(RefreshProductsJob.class, cronSchedule("0 0 0 * * ?"));
    }

    private void scheduleDetectTransactionIndexDivergence() {
        // Every hour (at the 38th minute)
        scheduleJob(DetectTransactionIndexDivergenceJob.class, cronSchedule("0 38 * * * ?"));
    }

    private void scheduleDeletePartiallyDeletedUsers() {
        // Every hour (at the 5th minute)
        scheduleJob(DeletePartiallyDeletedUsersJob.class, cronSchedule("0 5 * * * ?"));
    }

    private void scheduleAuthenticationCleanup() {
        // Every 10 minutes.
        scheduleJob(AuthenticationCleanupJob.class, cronSchedule("0 0/10 * * * ?"));
    }

    private void scheduleFastTraining() {
        // Every Midnight
        scheduleJob(FastTextTrainingJob.class, cronSchedule("0 0 0 * * ?"));
    }

    @Override
    public void stop() throws Exception {
        if (scheduler != null) {
            scheduler.shutdown(true);
            scheduler = null;
        }
    }

    private void scheduleJob(Class<? extends SystemCronJob> jobClass, CronScheduleBuilder cronSchedule) {
        final String jobName = "job-" + jobClass.getSimpleName();
        final String groupName = "group-" + jobName;
        final String triggerName = "trigger-" + jobName;

        try {
            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, groupName).build();

            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName)
                    .withSchedule(cronSchedule).forJob(jobName, groupName)
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (ObjectAlreadyExistsException e) {
            log.warn("Could not reschedule job: " + jobName);
        } catch (SchedulerException e) {
            log.error("Could not schedule system job: " + jobName, e);
        }
    }

    /**
     * Scheduled job that once a day triggers an automatic refresh for credentials with authentication error.
     */
    public static class AutomaticRefreshFailedCredentialsJob extends SystemCronJob {
        public AutomaticRefreshFailedCredentialsJob() {
            super(AutomaticRefreshFailedCredentialsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().refreshFailedCredentials();
        }
    }

    /**
     * Scheduled job that resets long running/frozen credentials refreshes.
     */
    public static class ResetHangingCredentialsJob extends SystemCronJob {
        public ResetHangingCredentialsJob() {
            super(ResetHangingCredentialsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().resetHangingCredentials();
        }
    }

    /**
     * Scheduled job that triggers the automatic refresh of credentials every minute.
     */
    public static class AutomaticRefreshJob extends SystemCronJob {
        public AutomaticRefreshJob() {
            super(AutomaticRefreshJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            RefreshCredentialSchedulationRequest request = new RefreshCredentialSchedulationRequest();
            request.setNow(context.getFireTime());
            request.setNextExecution(context.getNextFireTime());
            systemServiceFactory.getCronService().refreshCredentials(request);
        }
    }

    /**
     * Scheduled job that triggers the automatic update of FB profiles.
     */
    public static class UpdateFacebookProfileJob extends SystemCronJob {
        public UpdateFacebookProfileJob() {
            super(UpdateFacebookProfileJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().updateFacebookProfiles(null);
        }
    }

    /**
     * Scheduled job that reports system metrics every night.
     */
    public static class ReportStatisticsJob extends SystemCronJob {
        public ReportStatisticsJob() {
            super(ReportStatisticsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().reportSystemStatistics();
        }
    }

    /**
     * Scheduled job that sets PERMANENT_ERROR status on older credentials where status is AUTHENTICATION_ERROR and
     * agent_events says no successful status
     */
    public static class PermanentErrorMarkerJob extends SystemCronJob {
        public PermanentErrorMarkerJob() {
            super(PermanentErrorMarkerJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().relabelOldAuthErrors();
        }
    }

    /**
     * Scheduled job that sends notifications than has been batched overnight.
     */
    public static class SendNotificationsJob extends SystemCronJob {
        public SendNotificationsJob() {
            super(SendNotificationsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendUnsentNotifications();
        }
    }

    /**
     * Scheduled job that sends a message (push or email) if credentials are failing
     */
    public static class SendMessageForFailingCredentialsJob extends SystemCronJob {
        public SendMessageForFailingCredentialsJob() {
            super(SendMessageForFailingCredentialsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendMessageForFailingCredentials();
        }
    }

    /**
     * Scheduled job that sends a push to remind of unhandled fraud warnings.
     */
    public static class SendFraudReminderJob extends SystemCronJob {
        public SendFraudReminderJob() {
            super(SendFraudReminderJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendFraudReminder();
        }
    }

    /**
     * Scheduled job that sends a push notification to remind the user to refresh their (manually refreshable)
     * credentials.
     */
    public static class SendManualRefreshReminderJob extends SystemCronJob {
        public SendManualRefreshReminderJob() {
            super(SendManualRefreshReminderJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendManualRefreshReminder();
        }
    }

    /**
     * Scheduled job that sends an email (recurrently) if a user has signed up but not added any credentials.
     */
    public static class SendUserActivationReminderJob extends SystemCronJob {
        public SendUserActivationReminderJob() {
            super(SendUserActivationReminderJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendUserActivationReminder();
        }
    }

    /**
     * Scheduled job that fetches all personnumbers from CreditSafe that has at least one record which is changed.
     * It then triggers a refreshInformation
     */
    public static class RefreshChangedFraudCredentialsJob extends SystemCronJob {
        public RefreshChangedFraudCredentialsJob() {
            super(RefreshChangedFraudCredentialsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().refreshChangedFraudCredentials();
        }
    }

    /**
     * Scheduled job that sends a push notification to users when we think that they've gotten their salary.
     */
    public static class SendPaydayReminderJob extends SystemCronJob {
        public SendPaydayReminderJob() {
            super(SendPaydayReminderJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendPaydayReminders();
        }
    }

    /**
     * Scheduled job that sends monthly email summary to users on the monthly break date for the user.
     */
    public static class SendMonthlySummaryEmailJob extends SystemCronJob {
        public SendMonthlySummaryEmailJob() {
            super(SendMonthlySummaryEmailJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendMonthlySummaryEmails(new SendMonthlyEmailsRequest());
        }
    }

    /**
     * Send fallback notifications for encrypted notifications that haven't been acknowledged by a client.
     */
    public static class SendFallbackNotificationJob extends SystemCronJob {
        public SendFallbackNotificationJob() {
            super(SendFallbackNotificationJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().sendFallbackNotifications();
        }
    }

    /**
     * Refresh applications (the status of submitted applications are changed externally and needs to be refreshed).
     */
    public static class RefreshApplicationsJob extends SystemCronJob {
        public RefreshApplicationsJob() {
            super(RefreshApplicationsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().refreshApplications();
        }
    }

    /**
     * Report applications.
     */
    public static class ApplicationsReportingJob extends SystemCronJob {
        public ApplicationsReportingJob() {
            super(ApplicationsReportingJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().applicationsReporting();
        }
    }

    /**
     * Refresh products (targeting, offering and duration).
     */
    public static class RefreshProductsJob extends SystemCronJob {
        public RefreshProductsJob() {
            super(RefreshProductsJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().refreshProducts();
        }
    }

    /**
     * Clean up users of OAuth2 clients
     */
    public static class CleanUpOAuth2ClientsUsers extends SystemCronJob {
        public CleanUpOAuth2ClientsUsers() {
            super(CleanUpOAuth2ClientsUsers.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().cleanOAuth2Users();
        }
    }

    /**
     * Detect diverging transactions in Cassandra and ElasticSearch.
     * Runs detection on a sample of users, and sends the results to Prometheus.
     * No automatic fix of the diverges are currently done.
     */
    public static class DetectTransactionIndexDivergenceJob extends SystemCronJob {
        public DetectTransactionIndexDivergenceJob() {
            super(DetectTransactionIndexDivergenceJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().detectTransactionIndexDivergence();
        }
    }

    /**
     * Delete users that have been partially deleted.
     */
    public static class DeletePartiallyDeletedUsersJob extends SystemCronJob {
        public DeletePartiallyDeletedUsersJob() {
            super(DeletePartiallyDeletedUsersJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().deletePartiallyDeletedUsers();
        }
    }

    public static class AuthenticationCleanupJob extends SystemCronJob {
        public AuthenticationCleanupJob() {
            super(AuthenticationCleanupJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().authenticationCleanup();
        }
    }

    public static class FastTextTrainingJob extends SystemCronJob {
        public FastTextTrainingJob() {
            super(FastTextTrainingJob.class, isCurrentInstanceLeader());
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            systemServiceFactory.getCronService().trainModel();
        }
    }
}
