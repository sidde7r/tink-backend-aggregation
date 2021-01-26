package se.tink.libraries.credentials.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.enums.CredentialsTypes;
import se.tink.libraries.field.rpc.Field;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credentials implements Cloneable {

    @SuppressWarnings("serial")
    private static class FieldsMap extends HashMap<String, String> {}

    private Date debugUntil;
    private long providerLatency;

    @JsonInclude(Include.NON_NULL)
    private String sensitiveDataSerialized;

    @JsonIgnore private String fieldsSerialized;
    private String id;
    private Date nextUpdate;
    private String payload;
    private String providerName;

    @JsonInclude(Include.NON_NULL)
    private String secretKey;

    private Date sessionExpiryDate;
    private CredentialsStatus status;
    private String statusPayload;
    private String statusPrompt;
    private Date statusUpdated;
    private String supplementalInformation;
    private CredentialsTypes type;
    private Date updated;
    private String userId;

    private void generateIdIfMissing() {
        if (id == null) {
            id = UUIDUtils.generateUUID();
        }
    }

    @Override
    public Credentials clone() {
        try {
            return (Credentials) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Deprecated
    public String getAdditionalInformation() {
        return getField(Field.Key.ADDITIONAL_INFORMATION);
    }

    public String getField(String key) {
        Map<String, String> fields = getFields();

        if (fields == null) {
            return null;
        }

        return fields.get(key);
    }

    public String getField(Field.Key field) {
        return getField(field.getFieldKey());
    }

    public Map<String, String> getFields() {
        if (Strings.isNullOrEmpty(fieldsSerialized)) {
            return Maps.newHashMap();
        }

        return SerializationUtils.deserializeFromString(fieldsSerialized, FieldsMap.class);
    }

    public String getFieldsSerialized() {
        return fieldsSerialized;
    }

    public String getId() {
        generateIdIfMissing();
        return this.id;
    }

    public Date getNextUpdate() {
        return nextUpdate;
    }

    public String getSensitiveDataSerialized() {
        return sensitiveDataSerialized;
    }

    public void setSensitiveDataSerialized(String sensitiveDataSerialized) {
        this.sensitiveDataSerialized = sensitiveDataSerialized;
    }

    @Deprecated
    public String getPassword() {
        return getField(Field.Key.PASSWORD);
    }

    public String getPayload() {
        return payload;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public Date getSessionExpiryDate() {
        return sessionExpiryDate;
    }

    public CredentialsStatus getStatus() {
        return this.status;
    }

    public String getStatusPayload() {
        return statusPayload;
    }

    public String getStatusPrompt() {
        return statusPrompt;
    }

    public Date getStatusUpdated() {
        return this.statusUpdated;
    }

    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    public CredentialsTypes getType() {
        return type;
    }

    public Date getUpdated() {
        return this.updated;
    }

    public String getUserId() {
        return this.userId;
    }

    @Deprecated
    public String getUsername() {
        return getField(Field.Key.USERNAME);
    }

    public Date getDebugUntil() {
        return debugUntil;
    }

    // @Deprecated
    public void setAdditionalInformation(String additionalInformation) {
        if (Strings.isNullOrEmpty(additionalInformation)) {
            return;
        }

        setField(Field.Key.ADDITIONAL_INFORMATION, additionalInformation);
    }

    public void setDebugUntil(Date debugUntil) {
        this.debugUntil = debugUntil;
    }

    public void setField(String key, String value) {
        Map<String, String> fields = getFields();

        if (fields == null) {
            fields = Maps.newHashMap();
        }

        if (value != null) {
            fields.put(key, value);
        }

        setFields(fields);
    }

    public void setField(Field.Key key, String value) {
        setField(key.getFieldKey(), value);
    }

    public void setFields(Map<String, String> fields) {
        this.fieldsSerialized = SerializationUtils.serializeToString(fields);
    }

    public void setFieldsSerialized(String fieldsSerialized) {
        this.fieldsSerialized = fieldsSerialized;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNextUpdate(Date nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    // @Deprecated
    public void setPassword(String password) {
        if (Strings.isNullOrEmpty(password)) {
            return;
        }

        setField(Field.Key.PASSWORD, password);
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setProviderName(String provider) {
        this.providerName = provider;
    }

    public void setSessionExpiryDate(Date sessionExpiryDate) {
        this.sessionExpiryDate = sessionExpiryDate;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status;
    }

    public void setStatusPayload(String statusPayload) {
        this.statusPayload = statusPayload;
    }

    public void setStatusPrompt(String statusPrompt) {
        this.statusPrompt = statusPrompt;
    }

    public void setStatusUpdated(Date statusUpdated) {
        this.statusUpdated = statusUpdated;
    }

    public void setSupplementalInformation(String supplementalInformation) {
        this.supplementalInformation = supplementalInformation;
    }

    public void setType(CredentialsTypes type) {
        this.type = type;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    // @Deprecated
    public void setUsername(String username) {
        if (Strings.isNullOrEmpty(username)) {
            return;
        }

        setField(Field.Key.USERNAME, username);
    }

    public long getProviderLatency() {
        return providerLatency;
    }

    public void setProviderLatency(long providerLatency) {
        this.providerLatency = providerLatency;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("debugUntil", debugUntil)
                .add("providerLatency", providerLatency)
                .add("sensitiveDataSerialized", sensitiveDataSerialized == null ? null : "***")
                .add("fieldsSerialized", fieldsSerialized == null ? null : "***")
                .add("id", id)
                .add("nextUpdate", nextUpdate)
                .add("payload", payload == null ? null : "***")
                .add("providerName", providerName)
                .add("secretKey", secretKey == null ? null : "***")
                .add("sessionExpiryDate", sessionExpiryDate)
                .add("status", status)
                .add("statusPayload", statusPayload)
                .add("statusPrompt", statusPrompt)
                .add("statusUpdated", statusUpdated)
                .add("supplementalInformation", supplementalInformation == null ? null : "***")
                .add("type", type)
                .add("updated", updated)
                .add("userId", userId)
                .toString();
    }
}
