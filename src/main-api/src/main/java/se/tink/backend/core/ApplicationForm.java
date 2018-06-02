package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.application.FieldEntry;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.backend.utils.guavaimpl.Predicates;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApplicationForm {

    private static final TypeReference<List<FieldEntry>> LIST_FIELD_ENTRY_TYPE = new TypeReference<List<FieldEntry>>() {
    };

    @Tag(1)
    private UUID applicationId;
    @Tag(2)
    @ApiModelProperty(name = "description", value = "A description of this Form.", example = "First, we need to know some more about...")
    private String description;
    @Tag(3)
    @Modifiable
    @ApiModelProperty(name = "fields", value = "A list of Fields to render on this Form.")
    private List<ApplicationField> fields;
    @Tag(4)
    @ApiModelProperty(name = "id", value = "A unique identifier of this Form.")
    private UUID id;
    @Exclude
    @JsonIgnore
    private String name;
    @Exclude
    @JsonIgnore
    private UUID parentId;
    @Tag(5)
    @ApiModelProperty(name = "status", value = "The status of the ApplicationForm")
    private ApplicationFormStatus status;
    @Tag(6)
    @ApiModelProperty(name = "title", value = "The title of this Form.", example = "Your residence")
    private String title;
    @Tag(7)
    @ApiModelProperty(name = "type", value = "The type of this Form.", example = "mortgage/real-estate", allowableValues = ApplicationFormType.DOCUMENTED)
    private String type;
    @Tag(8)
    private UUID userId;
    @Tag(9)
    @JsonProperty(value = "payload")
    @ApiModelProperty(name = "payload", value="A payload can be given in order to layout custom views", example = "null")
    private String serializedPayload;

    public ApplicationForm() {
        id = UUID.randomUUID();
    }

    public ApplicationForm(ApplicationFormRow row) {
        this.userId = UUIDUtils.fromTinkUUID(row.getUserId());
        this.applicationId = UUIDUtils.fromTinkUUID(row.getApplicationId());
        this.id = UUIDUtils.fromTinkUUID(row.getId());
        this.parentId = (row.getParentId() != null) ? UUIDUtils.fromTinkUUID(row.getParentId()) : null;
        this.name = row.getName();
        this.type = row.getType();

        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setKey(ApplicationFormStatusKey.valueOf(row.getStatus()));
        status.setUpdated(row.getUpdated());
        this.status = status;

        List<ApplicationField> fields = Lists.newArrayList();
        List<FieldEntry> valuesByName = SerializationUtils.deserializeFromString(row.getFields(), LIST_FIELD_ENTRY_TYPE);
        for (FieldEntry entry : valuesByName) {
            ApplicationField field = new ApplicationField();
            field.setName(entry.getName());
            field.setValue(entry.getValue());
            fields.add(field);
        }

        this.fields = fields;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ApplicationField> getFields() {
        return fields;
    }

    public void setFields(List<ApplicationField> fields) {
        this.fields = fields;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ApplicationFormType getType() {
        if (type == null) {
            return null;
        } else {
            return ApplicationFormType.fromScheme(type);
        }
    }

    public void setType(ApplicationFormType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public ApplicationFormStatus getStatus() {
        return status;
    }
    
    public boolean hasError() {
        if (status == null) {
            return false;
        }
        
        return Objects.equals(status.getKey(), ApplicationFormStatusKey.ERROR);
    }

    public void setStatus(ApplicationFormStatus status) {
        this.status = status;
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

    public String getSerializedPayload() {
        return serializedPayload;
    }

    public void setSerializedPayload(String serializedPayload) {
        this.serializedPayload = serializedPayload;
    }

    @JsonIgnore
    public Optional<ApplicationField> getField(String fieldName) {
        return getFields().stream().filter(field -> Predicates.applicationFieldOfName(fieldName).apply(field))
                .findFirst();
    }
    
    @JsonIgnore
    public Optional<String> getFieldValue(String fieldName) {
        Optional<ApplicationField> field = getField(fieldName);
        
        if (field.isPresent()) {
            return Optional.ofNullable(field.get().getValue());
        } else {
            return Optional.empty();
        }
    }

    public ApplicationFormRow toRow(int formNumber) {
        ApplicationFormRow row = new ApplicationFormRow();
        row.setUserId(UUIDUtils.toTinkUUID(userId));
        row.setApplicationId(UUIDUtils.toTinkUUID(applicationId));
        row.setId(UUIDUtils.toTinkUUID(id));
        row.setName(name);
        row.setParentId(UUIDUtils.toTinkUUID(parentId));
        row.setStatus(status.getKey().toString());
        row.setUpdated(status.getUpdated());
        row.setType(type);
        row.setFormNumber(formNumber);

        List<FieldEntry> valueByName = Lists.newArrayList();
        for (ApplicationField field : fields) {
            valueByName.add(new FieldEntry(field.getName(), field.getValue()));
        }

        row.setFields(SerializationUtils.serializeToString(valueByName));

        return row;
    }

    public void updateStatus(ApplicationFormStatusKey status) {
        getStatus().setKey(status);
        getStatus().setUpdated(new Date());
    }
    
    public void updateStatus(ApplicationFormStatusKey status, String message) {
        updateStatus(status);
        getStatus().setMessage(message);
    }
    
    /*
     * Update the status key (and timestamp) only if it changed. Otherwise just update the message (which doesn't affect
     * the persisted entity).
     */
    public void updateStatusIfChanged(ApplicationFormStatusKey status, String message) {
        if (!Objects.equals(status, getStatus().getKey())) {
            updateStatus(status);
        }
        getStatus().setMessage(message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }
    
    /**
     * Populate this form with field values from another form.
     */
    @JsonIgnore
    public void populateValues(ApplicationForm source, PopulateValueMode readOnlyMode) {
        if (source.getFields() == null) {
            return;
        } 
        
        for (ApplicationField sourceField : source.getFields()) {
            Optional<ApplicationField> thisField = getField(sourceField.getName());
            
            if (thisField.isPresent()) {
                ApplicationField applicationField = thisField.get();

                if (Objects.equals(readOnlyMode, PopulateValueMode.SKIP_READ_ONLY) && applicationField.isReadOnly()) {
                    continue;
                }

                applicationField.setValue(sourceField.getValue());
            }
        }
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("applicationId", applicationId)
                .add("userId", userId)
                .add("type", type)
                .add("name", name)
                .add("title", title)
                .add("description", description)
                .add("status", status)
                .add("fields", fields)
                .add("serializedPayload", serializedPayload)
                .toString();
    }

    public enum PopulateValueMode {
        SKIP_READ_ONLY,
        WRITE_READ_ONLY
    }
}
