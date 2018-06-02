package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.utils.guavaimpl.Predicates;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Application {

    @Exclude
    @JsonIgnore
    private Date created;
    @Tag(1)
    @ApiModelProperty(name = "forms", value = "A list of Forms attached to this Application.", required = true)
    private List<ApplicationForm> forms;
    @Tag(2)
    @ApiModelProperty(name = "id", value = "The Application ID", required = true, example = "89cfe91ac88341e2b9f00e6f85280392")
    private UUID id;
    @Tag(3)
    @ApiModelProperty(name = "status", value = "The status of the Application")
    private ApplicationStatus status;
    @Tag(4)
    @ApiModelProperty(name = "steps", value = "The number of steps in this Application. Can be increased based on answers in submitted forms", example = "5")
    private int steps;
    @Tag(5)
    @ApiModelProperty(name = "title", value = "A title of the Application", example = "Move mortgage")
    private String title;
    @Tag(6)
    @ApiModelProperty(name = "type", value = "The type of this Application.", required = true, example = "mortgage/switch-provider", allowableValues = ApplicationType.DOCUMENTED)
    private String type;
    @Tag(7)
    @ApiModelProperty(name = "userId", value = "The User ID", required = true, example = "b90d563cbc2d4df3970a3c8bd1fcbdab")
    private UUID userId;
    @Exclude
    @JsonIgnore
    private Optional<ProductArticle> productArticle = Optional.empty();
    @Exclude
    @JsonIgnore
    private HashMap<ApplicationPropertyKey, Object> properties = Maps.newHashMap();
    
    private static final TypeReference<HashMap<ApplicationPropertyKey, Object>> PROPERTIES_HASHMAP_TYPE_REFERENCE = new TypeReference<HashMap<ApplicationPropertyKey, Object>>() {
    };
    

    public Application() {
        id = UUID.randomUUID();
    }

    public Application(ApplicationRow row) {
        this.created = row.getCreated();
        this.id = UUIDUtils.fromTinkUUID(row.getId());
        this.type = row.getType();
        this.userId = UUIDUtils.fromTinkUUID(row.getUserId());
        this.type = row.getType();

        ApplicationStatus status = new ApplicationStatus();
        status.setKey(ApplicationStatusKey.valueOf(row.getStatus()));
        status.setUpdated(row.getUpdated());
        this.status = status;
        
        if (!Strings.isNullOrEmpty(row.getProperties())) {
            this.properties = SerializationUtils.deserializeFromString(row.getProperties(),
                    PROPERTIES_HASHMAP_TYPE_REFERENCE);
        }
    }

    public ApplicationType getType() {
        if (type == null) {
            return null;
        } else {
            return ApplicationType.fromScheme(type);
        }
    }

    public void setType(ApplicationType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public List<ApplicationForm> getForms() {
        return forms;
    }

    public void setForms(List<ApplicationForm> forms) {
        this.forms = forms;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    
    public Optional<ProductArticle> getProductArticle() {
        return productArticle;
    }
    
    public void setProductArticle(ProductArticle productArticle) {
        if (productArticle == null) {
            this.productArticle = Optional.empty();
        } else {
            this.productArticle = Optional.of(productArticle);
        }
    }
    
    public HashMap<ApplicationPropertyKey, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(HashMap<ApplicationPropertyKey, Object> properties) {
        this.properties = properties;
    }

    public ApplicationRow toRow() {
        ApplicationRow row = new ApplicationRow();
        row.setCreated(created);
        row.setId(UUIDUtils.toTinkUUID(id));
        row.setStatus(status.getKey().toString());
        row.setUpdated(status.getUpdated());
        row.setType(type);
        row.setUserId(UUIDUtils.toTinkUUID(userId));
        row.setProperties(SerializationUtils.serializeToString(properties));

        return row;
    }

    @JsonIgnore
    public Optional<ApplicationForm> getFirstForm(String formName) {
        List<ApplicationForm> forms = getForms(formName);
        
        if (forms.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(forms.get(0));
    }
    
    @JsonIgnore
    public Optional<ApplicationForm> getForm(UUID formId) {
        return getForms().stream().filter(form -> Predicates.applicationFormById(formId).apply(form)).findFirst();
    }
    
    @JsonIgnore
    public List<ApplicationForm> getForms(String formName) {
        return Lists.newArrayList(Iterables.filter(getForms(), Predicates.applicationFormOfName(formName)));
    }

    public void attach(ApplicationForm form) {
        forms.add(form);

        status.setUpdated(new Date());
        status.setKey(ApplicationStatusKey.IN_PROGRESS);
    }

    @JsonIgnore
    public List<ApplicationForm> getFormNamesByParentId(UUID parentId, boolean recursive) {
        List<ApplicationForm> forms = Lists.newArrayList();
        getFormNamesByParentId(forms, parentId, recursive);
        return forms;
    }

    private void getFormNamesByParentId(List<ApplicationForm> collection, UUID parentId, boolean recursive) {
        for (ApplicationForm form : forms) {
            if (parentId.equals(form.getParentId())) {
                collection.add(form);
                if (recursive) {
                    getFormNamesByParentId(collection, form.getId(), recursive);
                }
            }
        }
    }

    public void updateStatus(ApplicationStatusKey key) {
        getStatus().setKey(key);
        getStatus().setUpdated(new Date());
    }
    
    public void updateStatus(ApplicationStatusKey key, String message) {
        updateStatus(key);
        getStatus().setMessage(message);
    }
    
    /*
     * Update the status key (and timestamp) only if it changed.
     */
    public boolean updateStatusIfChanged(ApplicationStatusKey key) {
        if (!Objects.equals(key, getStatus().getKey())) {
            updateStatus(key);
            return true;
        }
        return false;
    }
    
    /*
     * Update the status key (and timestamp) only if it changed. Otherwise just update the message (which doesn't affect
     * the persisted entity).
     */
    public void updateStatusIfChanged(ApplicationStatusKey key, String message) {
        updateStatusIfChanged(key);
        getStatus().setMessage(message);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("type", type)
                .add("created", created)
                .add("title", title)
                .add("steps", steps)
                .add("status", status)
                .add("forms", forms)
                .toString();
    }
}
