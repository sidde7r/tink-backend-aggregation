package se.tink.libraries.application;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.UUID;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class GenericApplication {
    private UUID applicationId;
    private UUID credentialsId;
    private List<GenericApplicationFieldGroup> fieldGroups;
    private String personalNumber;
    private UUID productId;
    private String type;
    private UUID userId;
    private String remoteIp;

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public List<GenericApplicationFieldGroup> getFieldGroups() {
        return fieldGroups;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public UUID getProductId() {
        return productId;
    }

    public ApplicationType getType() {
        if (type == null) {
            return null;
        } else {
            return ApplicationType.fromScheme(type);
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setFieldGroups(List<GenericApplicationFieldGroup> fieldGroups) {
        this.fieldGroups = fieldGroups;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setType(ApplicationType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("applicationId", applicationId)
                .add("credentialsId", credentialsId)
                .add("productId", productId)
                .add("userId", userId)
                .add("personalNumber", personalNumber)
                .add("type", type)
                .add("fieldGroups", fieldGroups)
                .toString();
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }
}
