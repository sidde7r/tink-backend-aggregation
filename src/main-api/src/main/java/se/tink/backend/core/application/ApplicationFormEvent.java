package se.tink.backend.core.application;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.serialization.TypeReferences;

@Table(value = "applications_forms_events")
public class ApplicationFormEvent {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID userId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID applicationId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
    private UUID formId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3)
    private UUID id;
    private String formName;
    private String formStatus;
    private Date formUpdated;
    private String formFieldDisplayErrors;

    public ApplicationFormEvent() {

    }

    public ApplicationFormEvent(ApplicationForm form) {
        this.userId = form.getUserId();
        this.applicationId = form.getApplicationId();
        this.formId = form.getId();
        this.id = UUIDs.timeBased();
        this.formName = form.getName();
        this.formStatus = form.getStatus().getKey().name();
        this.formUpdated = form.getStatus().getUpdated();
        this.formFieldDisplayErrors = SerializationUtils.serializeToString(createFormFieldDisplayErrorsMap(form));
    }

    /**
     * Create a map of names and `null` if no errors, if any errors value is the display error.
     *
     * This can also of course be used to analyze the fields that have been displayed.
     */
    private static Map<String, String> createFormFieldDisplayErrorsMap(ApplicationForm form) {
        List<ApplicationField> formFields = form.getFields();

        if (formFields == null) {
            return Maps.newHashMap();
        }

        Map<String, String> map = Maps.newHashMap();
        for (ApplicationField formField : formFields) {
            map.put(formField.getName(), formField.getDisplayError());
        }

        return map;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getFormId() {
        return formId;
    }

    public void setFormId(UUID formId) {
        this.formId = formId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public ApplicationFormStatusKey getFormStatus() {
        return formStatus != null ? ApplicationFormStatusKey.valueOf(formStatus) : null;
    }

    public void setFormStatus(ApplicationFormStatusKey formStatus) {
        this.formStatus = formStatus != null ? formStatus.name() : null;
    }

    public Date getFormUpdated() {
        return formUpdated;
    }

    public void setFormUpdated(Date formUpdated) {
        this.formUpdated = formUpdated;
    }

    public Map<String, String> getFormFieldDisplayErrors() {
        if (Strings.isNullOrEmpty(formFieldDisplayErrors)) {
            return Maps.newHashMap();
        } else {
            return SerializationUtils
                    .deserializeFromString(formFieldDisplayErrors, TypeReferences.MAP_OF_STRING_STRING);
        }
    }

    public void setFormFieldDisplayErrors(Map<String, String> formFieldDisplayErrors) {
        this.formFieldDisplayErrors = formFieldDisplayErrors != null ? SerializationUtils.serializeToString(
                formFieldDisplayErrors) : null;
    }
}
