package se.tink.backend.core.application;

import com.datastax.driver.core.utils.UUIDs;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationEventTest {
    @Test
    public void createApplicationEventFromApplication() {
        Application application = createApplication();

        ApplicationEvent applicationEvent = new ApplicationEvent(application);
        assertThat(applicationEvent.getUserId()).isEqualTo(application.getUserId());
        assertThat(applicationEvent.getApplicationId()).isEqualTo(application.getId());
        assertThat(applicationEvent.getId()).isNotNull();
        assertThat(applicationEvent.getApplicationStatus()).isEqualTo(application.getStatus().getKey());
        assertThat(applicationEvent.getApplicationType()).isEqualTo(application.getType());
        assertThat(applicationEvent.getApplicationUpdated()).isEqualTo(application.getStatus().getUpdated());
    }

    @Test
    public void applicationEventHasTimestampBasedId() {
        Application application = createApplication();

        UUID preInstantiate = UUIDs.timeBased();
        ApplicationEvent applicationEvent = new ApplicationEvent(application);
        UUID postInstantiate = UUIDs.timeBased();

        assertThat(applicationEvent.getId().timestamp())
                .isGreaterThan(preInstantiate.timestamp())
                .isLessThan(postInstantiate.timestamp());
    }

    private Application createApplication() {
        ApplicationStatus status = new ApplicationStatus();
        status.setKey(ApplicationStatusKey.COMPLETED);
        status.setUpdated(new Date());

        Application application = new Application();
        application.setStatus(status);
        application.setUserId(UUID.randomUUID());
        application.setId(UUID.randomUUID());
        application.setType(ApplicationType.SWITCH_MORTGAGE_PROVIDER);
        return application;
    }
}
