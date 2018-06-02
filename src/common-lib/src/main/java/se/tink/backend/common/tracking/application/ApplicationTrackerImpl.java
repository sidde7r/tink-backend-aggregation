package se.tink.backend.common.tracking.application;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.application.ApplicationEvent;
import se.tink.backend.core.application.ApplicationFormEvent;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.enums.ApplicationStatusKey;

public class ApplicationTrackerImpl implements ApplicationTracker {

    private static final MetricId METRIC_APPLICATION_STATUS_CHANGED = MetricId.newId("application_status_changed");

    private final ApplicationEventRepository applicationEventRepository;
    private final ApplicationFormEventRepository applicationFormEventRepository;
    private final MetricRegistry registry;

    public ApplicationTrackerImpl(
            ApplicationEventRepository applicationEventRepository,
            ApplicationFormEventRepository applicationFormEventRepository,
            MetricRegistry registry) {
        this.applicationEventRepository = applicationEventRepository;
        this.applicationFormEventRepository = applicationFormEventRepository;
        this.registry = registry;
    }

    @Override
    public void track(Application application) {
        track(application, null);
    }

    @Override
    public void track(Application application, ApplicationForm form) {
        List<ApplicationEvent> mostRecentEvents = applicationEventRepository
                .findMostRecentByUserIdAndApplicationId(application.getUserId(), application.getId(), 1);

        if (!shouldSaveNewApplicationEvent(mostRecentEvents, application)) {
            return;
        }

        applicationEventRepository.save(new ApplicationEvent(application));

        if (form != null) {
            applicationFormEventRepository.save(new ApplicationFormEvent(form));
        }

        if (isApplicationStatusChanged(mostRecentEvents, application)) {
            registry.meter(METRIC_APPLICATION_STATUS_CHANGED
                    .label("type", application.getType().name())
                    .label("provider", getProviderName(application))
                    .label("new_status", application.getStatus().getKey().name())
                    .label("old_status", mostRecentEvents.isEmpty() ?
                            null :
                            mostRecentEvents.get(0).getApplicationStatus().name()))
                    .inc();
        }
    }

    private boolean shouldSaveNewApplicationEvent(List<ApplicationEvent> mostRecentEvents, Application application) {
        if (mostRecentEvents.isEmpty()) {
            return true;
        }

        Date applicationUpdated = application.getStatus().getUpdated();
        Date mostRecentEventUpdated = mostRecentEvents.get(0).getApplicationUpdated();

        return applicationUpdated.after(mostRecentEventUpdated);
    }

    private boolean isApplicationStatusChanged(List<ApplicationEvent> mostRecentEvents, Application application) {
        if (mostRecentEvents.isEmpty()) {
            return true;
        }

        ApplicationStatusKey newApplicationStatus = application.getStatus().getKey();
        ApplicationStatusKey latestApplicationStatus = mostRecentEvents.get(0).getApplicationStatus();

        return !Objects.equals(newApplicationStatus, latestApplicationStatus);
    }

    private String getProviderName(Application application) {
        // FIXME: Remove this first null check if safe
        HashMap<ApplicationPropertyKey, Object> applicationProperties = application.getProperties();
        if (applicationProperties == null) {
            return null;
        }

        String providerName = (String) applicationProperties.get(ApplicationPropertyKey.PRODUCT_PROVIDER_NAME);

        if (Strings.isNullOrEmpty(providerName)) {
            return null;
        }

        return providerName;
    }
}
