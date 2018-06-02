package se.tink.backend.system.controllers;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BackOfficeNotificationController {

    private static final ImmutableSet<ApplicationStatusKey> CANCELLATION_STATUSES = ImmutableSet
            .of(ApplicationStatusKey.REJECTED, ApplicationStatusKey.ABORTED);
    private static final Joiner HTML_LINE_JOINER = Joiner.on("<br>");
    private static final LogUtils log = new LogUtils(BackOfficeNotificationController.class);

    private final BackOfficeConfiguration backOfficeConfiguration;
    private final MailSender mailSender;

    public BackOfficeNotificationController(MailSender mailSender, BackOfficeConfiguration backOfficeConfiguration) {
        this.mailSender = mailSender;
        this.backOfficeConfiguration = backOfficeConfiguration;
    }

    public boolean notifyBackOfficeAboutApplicationStatus(Application application) {
        return notifyBackOfficeAboutApplicationStatus(
                String.valueOf(application.getProperties().get(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID)),
                application.getStatus());
    }

    boolean notifyBackOfficeAboutApplicationStatus(String exernalApplicationId, ApplicationStatus status) {
        try {
            List<String> body = Lists.newArrayList();
            body.add(Catalog.format("Application was {0}", getBodyStatus(status)));
            body.add("");
            body.add(Catalog.format("External reference: {0}", exernalApplicationId));
            body.add(Catalog.format("Status: {0}", status.getKey().name()));
            body.add(Catalog.format("Updated: {0}",
                    ThreadSafeDateFormat.FORMATTER_SECONDS_WITH_TIMEZONE.format(status.getUpdated())));

            boolean emailWasSent = mailSender.sendMessage(
                    backOfficeConfiguration.getToAddress(),
                    Catalog.format("{0}: {1}", getSubjectStatus(status), exernalApplicationId),
                    backOfficeConfiguration.getFromAddress(),
                    backOfficeConfiguration.getFromName(),
                    HTML_LINE_JOINER.join(body),
                    true);

            return emailWasSent;
        } catch (Exception e) {
            log.error("Unable to send email.", e);
            return false;
        }
    }

    private String getSubjectStatus(ApplicationStatus status) {
        if (CANCELLATION_STATUSES.contains(status.getKey())) {
            return "CANCELLED";
        }

        return status.getKey().name().toUpperCase();
    }

    private String getBodyStatus(ApplicationStatus status) {
        if (CANCELLATION_STATUSES.contains(status.getKey())) {
            return "cancelled";
        }

        return status.getKey().name().toLowerCase();
    }
}
