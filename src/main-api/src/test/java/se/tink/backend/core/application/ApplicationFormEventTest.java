package se.tink.backend.core.application;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationFormEventTest {
    @Test
    public void createApplicationFormEventFromApplication() {
        ApplicationForm applicationForm = createForm();

        ApplicationFormEvent applicationEvent = new ApplicationFormEvent(applicationForm);
        assertThat(applicationEvent.getUserId()).isEqualTo(applicationForm.getUserId());
        assertThat(applicationEvent.getApplicationId()).isEqualTo(applicationForm.getApplicationId());
        assertThat(applicationEvent.getFormId()).isEqualTo(applicationForm.getId());
        assertThat(applicationEvent.getId()).isNotNull();
        assertThat(applicationEvent.getFormStatus()).isEqualTo(applicationForm.getStatus().getKey());
        assertThat(applicationEvent.getFormName()).isEqualTo(applicationForm.getName());
        assertThat(applicationEvent.getFormUpdated()).isEqualTo(applicationForm.getStatus().getUpdated());
        assertThat(applicationEvent.getFormFieldDisplayErrors()).isEmpty();
    }

    @Test
    public void createApplicationFormEventFromApplicationWithFields_setsFieldNamesAndDisplayErrors() {
        ApplicationForm applicationForm = createForm();

        // Create some dummy fields that we want to store
        ApplicationField applicationField1 = new ApplicationField();
        applicationField1.setName("Field 1");
        applicationField1.setValue("First value");

        ApplicationField applicationField2 = new ApplicationField();
        applicationField2.setName("Field 2");
        applicationField2.setValue("Second value");
        applicationField2.setDisplayError("Error on second field.");

        applicationForm.setFields(Lists.newArrayList(applicationField1, applicationField2));

        ApplicationFormEvent applicationEvent = new ApplicationFormEvent(applicationForm);
        assertThat(applicationEvent.getFormFieldDisplayErrors()).hasSize(2);

        assertThat(applicationEvent.getFormFieldDisplayErrors().get("Field 1")).isNull();
        assertThat(applicationEvent.getFormFieldDisplayErrors().get("Field 2")).isEqualTo("Error on second field.");
    }

    private static ApplicationForm createForm() {
        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setKey(ApplicationFormStatusKey.COMPLETED);
        status.setUpdated(new Date());

        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setStatus(status);
        applicationForm.setUserId(UUID.randomUUID());
        applicationForm.setId(UUID.randomUUID());
        applicationForm.setName("Form name");

        return applicationForm;
    }

    @Test
    public void applicationFormEventHasTimestampBasedId() {
        ApplicationForm applicationForm = createForm();

        UUID preInstantiate = UUIDs.timeBased();
        ApplicationFormEvent applicationFormEvent = new ApplicationFormEvent(applicationForm);
        UUID postInstantiate = UUIDs.timeBased();

        assertThat(applicationFormEvent.getId().timestamp())
                .isGreaterThan(preInstantiate.timestamp())
                .isLessThan(postInstantiate.timestamp());
    }
}
