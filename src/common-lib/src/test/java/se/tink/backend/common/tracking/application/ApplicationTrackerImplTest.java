package se.tink.backend.common.tracking.application;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.application.ApplicationEvent;
import se.tink.backend.core.application.ApplicationFormEvent;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ApplicationTrackerImplTest {
    @Test
    public void savesOnlyApplicationEventIfNoForm() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        ApplicationFormEventRepository mockedFormEventRepository = mock(ApplicationFormEventRepository.class);
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(new Counter());

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mockedFormEventRepository, mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(applicationUpdatedDate);

        // Mock behavior
        ApplicationEvent existingApplicationEvent = new ApplicationEvent(createApplication(
                applicationUpdatedDate.minusDays(1)));
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.newArrayList(existingApplicationEvent));

        // Run tracking
        applicationTracker.track(application);

        // Verify we save new event
        verify(mockedEventRepository, times(1))
                .save(any(ApplicationEvent.class));
        verifyZeroInteractions(mockedFormEventRepository);
    }

    @Test
    public void savesEventIfNoEventsInDb() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        ApplicationFormEventRepository mockedFormEventRepository = mock(ApplicationFormEventRepository.class);
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(new Counter());

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mockedFormEventRepository, mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(applicationUpdatedDate);

        // Mock behavior
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.<ApplicationEvent>newArrayList());

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // Verify we save new event
        verify(mockedEventRepository, times(1))
                .save(any(ApplicationEvent.class));
        verify(mockedFormEventRepository, times(1))
                .save(any(ApplicationFormEvent.class));
    }

    @Test
    public void savesEventIfUpdatedDateIsNewerThanDb() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        ApplicationFormEventRepository mockedFormEventRepository = mock(ApplicationFormEventRepository.class);
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(new Counter());

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mockedFormEventRepository, mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(applicationUpdatedDate);

        // Mock behavior
        ApplicationEvent existingApplicationEvent = new ApplicationEvent(createApplication(
                applicationUpdatedDate.minusDays(1)));
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.newArrayList(existingApplicationEvent));

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // Verify we save new event
        verify(mockedEventRepository, times(1))
                .save(any(ApplicationEvent.class));
        verify(mockedFormEventRepository, times(1))
                .save(any(ApplicationFormEvent.class));
    }

    @Test
    public void doesNotSaveEventIfUpdatedDateIsNotNewerThanDb() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        ApplicationFormEventRepository mockedFormEventRepository = mock(ApplicationFormEventRepository.class);
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(new Counter());

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mockedFormEventRepository, mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(applicationUpdatedDate);

        // Mock behavior
        ApplicationEvent existingApplicationEvent = new ApplicationEvent(createApplication(applicationUpdatedDate));
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.newArrayList(existingApplicationEvent));

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // We shouldn't save new event
        verify(mockedEventRepository, times(0))
                .save(any(ApplicationEvent.class));
        verify(mockedFormEventRepository, times(0))
                .save(any(ApplicationFormEvent.class));
    }

    @Test
    public void measuresStatusChangeIfNoEventsInDb() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        Counter counter = new Counter();
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(counter);

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mock(ApplicationFormEventRepository.class), mockedMetricRegistry);

        Application application = createApplication(new DateTime("2016-01-01"));

        // Mock behavior
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.<ApplicationEvent>newArrayList());

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // Verify we increase counter
        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void measuresStatusChangeIfRecentEventIsOlderAndNewStatus() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        Counter counter = new Counter();
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(counter);

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mock(ApplicationFormEventRepository.class), mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(ApplicationStatusKey.SIGNED, applicationUpdatedDate);

        // Mock behavior
        ApplicationEvent existingApplicationEvent = new ApplicationEvent(createApplication(
                ApplicationStatusKey.IN_PROGRESS,
                applicationUpdatedDate.minusDays(1)));
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.newArrayList(existingApplicationEvent));

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // Verify we increase counter
        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void doesNotMeasuresStatusChangeIfRecentEventIsSameStatus() {
        ApplicationEventRepository mockedEventRepository = mock(ApplicationEventRepository.class);
        Counter counter = new Counter();
        MetricRegistry mockedMetricRegistry = mockMetricRegistry(counter);

        ApplicationTrackerImpl applicationTracker = new ApplicationTrackerImpl(mockedEventRepository,
                mock(ApplicationFormEventRepository.class), mockedMetricRegistry);

        DateTime applicationUpdatedDate = new DateTime("2016-01-01");
        Application application = createApplication(ApplicationStatusKey.IN_PROGRESS, applicationUpdatedDate);

        // Mock behavior
        ApplicationEvent existingApplicationEvent = new ApplicationEvent(createApplication(
                ApplicationStatusKey.IN_PROGRESS,
                applicationUpdatedDate.minusDays(1)));
        when(mockedEventRepository.findMostRecentByUserIdAndApplicationId(
                eq(application.getUserId()), eq(application.getId()), anyInt()))
                .thenReturn(Lists.newArrayList(existingApplicationEvent));

        // Run tracking
        applicationTracker.track(application, createForm(application));

        // Verify we increase counter
        assertThat(counter.getCount()).isEqualTo(0);
    }

    private static ApplicationForm createForm(Application application) {
        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setUpdated(application.getStatus().getUpdated());
        status.setKey(ApplicationFormStatusKey.COMPLETED);

        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setUserId(application.getUserId());
        applicationForm.setStatus(status);
        applicationForm.setId(UUID.randomUUID());
        applicationForm.setApplicationId(application.getId());
        applicationForm.setType(ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_APPLICANT);
        return applicationForm;
    }

    private static Application createApplication(DateTime updatedDate) {
        return createApplication(ApplicationStatusKey.CREATED, updatedDate);
    }

    private static Application createApplication(ApplicationStatusKey applicationStatusKey, DateTime updatedDate) {
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setUpdated(updatedDate.toDate());
        applicationStatus.setKey(applicationStatusKey);

        Application application = new Application();
        application.setId(UUIDs.timeBased());
        application.setUserId(UUID.randomUUID());
        application.setStatus(applicationStatus);
        application.setType(ApplicationType.SWITCH_MORTGAGE_PROVIDER);

        return application;
    }

    private MetricRegistry mockMetricRegistry(Counter counter) {
        MetricRegistry mockedMetricRegistry = mock(MetricRegistry.class);
        when(mockedMetricRegistry.meter(any(MetricId.class))).thenReturn(counter);
        return mockedMetricRegistry;
    }
}
