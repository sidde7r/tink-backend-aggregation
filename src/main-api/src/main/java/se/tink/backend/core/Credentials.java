package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

@Entity
@Table(name = "credentials")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Credentials implements Cloneable {

    @SuppressWarnings("serial")
    private static class FieldsMap extends HashMap<String, String> {

    }

    private static final ImmutableSet<Field.Key> TRIM_FIELDS_WHITELIST = ImmutableSet.of(Field.Key.USERNAME);

    private static final Minutes KEEP_ALIVE_MAX_AGE = Minutes.minutes(30);

    private static final Minutes KEEP_ALIVE_MIN_AGE = Minutes.minutes(2);

    @Exclude
    @ApiModelProperty(name = "debugUntil", hidden = true)
    private Date debugUntil;
    @Transient
    @Exclude
    @ApiModelProperty(name = "providerLatency", hidden = true)
    private long providerLatency;

    @JsonInclude(Include.NON_NULL)
    @Creatable
    @Modifiable
    @Type(type = "text")
    @Exclude
    @ApiModelProperty(name = "sensitiveDataSerialized", hidden = true)
    private String sensitiveDataSerialized;

    @JsonIgnore
    @Creatable
    @Modifiable
    @Column(name = "`fields`")
    @Type(type = "text")
    @Tag(9)
    private String fieldsSerialized;
    @Id
    @Tag(1)
    @ApiModelProperty(name = "id", value = "The id of the credentials.", example = "6e68cc6287704273984567b3300c5822")
    private String id;
    @Exclude
    @ApiModelProperty(name = "nextUpdate", hidden = true)
    private Date nextUpdate;
    @Creatable
    @Type(type = "text")
    @Exclude
    @ApiModelProperty(name = "payload", hidden = true)
    private String payload;
    @Creatable
    @Tag(2)
    @ApiModelProperty(name = "providerName", value = "The provider (financial institute) that the credentials belongs to.", example = "handelsbanken-bankid", required = true)
    private String providerName;
    @JsonInclude(Include.NON_NULL)
    @Type(type = "text")
    @Exclude
    @ApiModelProperty(name = "secretKey", hidden = true)
    private String secretKey;
    @Creatable
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Tag(4)
    @ApiModelProperty(name = "status", value = "The status of the credentials.", allowableValues = CredentialsStatus.DOCUMENTED, example = "UPDATED")
    private CredentialsStatus status;
    @Modifiable
    @Type(type = "text")
    @Tag(6)
    @ApiModelProperty(name = "statusPayload", value = "A user-friendly message connected to the status. Could be an error message or text describing what is currently going on in the refresh process.", example = "Analyzed 1,200 out of 1,200 transactions.")
    private String statusPayload;
    @Type(type = "text")
    @Tag(7)
    @ApiModelProperty(name = "statusPrompt", hidden = true)
    private String statusPrompt;
    @Tag(5)
    @ApiModelProperty(name = "statusUpdated", value = "A timestamp of when the credentials' status was last updated.", example = "1493379467000")
    @Column(columnDefinition = "DATETIME(6)")
    private Date statusUpdated;
    @Creatable
    @Modifiable
    @Type(type = "text")
    @Tag(10)
    @ApiModelProperty(name = "supplementalInformation", value = "A key-value structure to handle if status of credentials are AWAITING_SUPPLEMENTAL_INFORMATION.", example = "null")
    private String supplementalInformation;
    @Creatable
    @Enumerated(EnumType.STRING)
    @Tag(3)
    @ApiModelProperty(name = "type", value = "The type of credentials.", allowableValues = CredentialsTypes.DOCUMENTED, example = "MOBILE_BANKID")
    private CredentialsTypes type;
    @Tag(8)
    @ApiModelProperty(name = "updated", value = "A timestamp of when the credentials was the last time in status UPDATED.", example = "1493379467000")
    @Column(columnDefinition = "DATETIME(6)")
    private Date updated;
    @Exclude
    @ApiModelProperty(name = "userId", value = "The id of the user that the credentials belongs to.", example = "c4ae034f96c740da91ae00022ddcac4d")
    private String userId;
    @Exclude
    @Transient
    @JsonIgnore // Shoudn't be used between containers.
    @ApiModelProperty(name = "sensitivePayloadSerialized", hidden = true)
    private String sensitivePayloadSerialized;

    @PrePersist
    private void generateIdIfMissing() {
        if (id == null) {
            id = StringUtils.generateUUID();
        }
    }

    public void addSerializedFields(String maskedFields) {
        Map<String, String> fields = getFields();
        Map<String, String> newFields = SerializationUtils.deserializeFromString(maskedFields, FieldsMap.class);
        fields.putAll(newFields);
        setFields(fields);
    }

    /**
     * Removes any information that is not to be stored in the main database.
     *
     * @param provider
     */
    public void clearSensitiveInformation(Provider provider) {
        setSensitivePayload(null);
        setFields(separateFields(provider, false));
    }

    /**
     * Removes any information that is not to be returned to the client.
     */
    public void clearInternalInformation(Provider provider) {
        clearSensitiveInformation(provider);
        setSecretKey(null);
        setPayload(null);
        setSensitiveDataSerialized(null);
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
    @ApiModelProperty(name = "additionalInformation", hidden = true)
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
    @ApiModelProperty(name = "password", hidden = true)
    public String getPassword() {
        return getField(Field.Key.PASSWORD);
    }

    public String getPayload() {
        return payload;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getSensitivePayloadSerialized() {
        return sensitivePayloadSerialized;
    }

    @ApiModelProperty(name = "sensitivePayload", hidden = true)
    public Map<String, String> getSensitivePayload() {
        if (Strings.isNullOrEmpty(getSensitivePayloadSerialized())) {
            return Maps.newHashMap();
        }

        Map<String, String> sensitivePayload = SerializationUtils.deserializeFromString(getSensitivePayloadSerialized(),
                new TypeReference<HashMap<String, String>>() {
                });

        // `sensitivePayload` is `null` if we're unable to deserialize the payload
        if (sensitivePayload == null) {
            sensitivePayload = Maps.newHashMap();
        }

        return sensitivePayload;
    }

    @ApiModelProperty(name = "sensitivePayload", hidden = true)
    public String getSensitivePayload(String key) {
        if (getSensitivePayload() == null) {
            return null;
        }

        return getSensitivePayload().get(key);
    }

    @ApiModelProperty(name = "sensitivePayload", hidden = true)
    public String getSensitivePayload(Field.Key key) {
        return getSensitivePayload(key.getFieldKey());
    }

    public <T> Optional<T> getSensitivePayload(Field.Key key, Class<T> cls) {
        String data = getSensitivePayload(key);

        if (Strings.isNullOrEmpty(data)) {
            return Optional.empty();
        }

        return Optional.ofNullable(SerializationUtils.deserializeFromString(data, cls));
    }

    public <T> Optional<T> getSensitivePayload(Field.Key key, TypeReference<T> typeReference) {
        String data = getSensitivePayload(key);

        if (Strings.isNullOrEmpty(data)) {
            return Optional.empty();
        }

        return Optional.ofNullable(SerializationUtils.deserializeFromString(data, typeReference));
    }

    public void removeSensitivePayload(Field.Key key) {
        Map<String, String> sensitivePayload = getSensitivePayload();

        if (sensitivePayload == null) {
            return;
        }

        sensitivePayload.remove(key.getFieldKey());

        setSensitivePayload(sensitivePayload);
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
    @ApiModelProperty(name = "sensitivePayload", hidden = true)
    public String getUsername() {
        return getField(Field.Key.USERNAME);
    }

    @JsonIgnore
    public boolean isDebug() {
        return debugUntil != null && debugUntil.after(new Date());
    }

    public Date getDebugUntil() {
        return debugUntil;
    }

    public void onlySensitiveInformation(Provider provider) {
        setFields(separateFields(provider, true));
    }

    private Map<String, String> separateFields(Provider provider, boolean masked) {
        Map<String, String> fields = Maps.newHashMap();

        Set<Entry<String, String>> credentialsFields = getFields().entrySet();
        List<Field> providerFields = provider.getFields();

        for (final Entry<String, String> fieldEntry : credentialsFields) {
            Field field = providerFields.stream().filter(f -> Objects.equal(fieldEntry.getKey(), f.getName()))
                    .findFirst().orElse(null);

            if (field != null && field.isMasked() == masked) {
                // Fields exists and has same config of masked as asked for
                fields.put(fieldEntry.getKey(), fieldEntry.getValue());
            }
        }

        return fields;
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

    @ApiModelProperty(name = "fields", value = "This is a key-value map of Field name and value found on the Provider to which the credentials belongs to. This parameter is required when creating credentials.", example = "{\"username\":\"198410045701\"}", required = true)
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

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setSensitivePayloadSerialized(String sensitivePayloadSerialized) {
        this.sensitivePayloadSerialized = sensitivePayloadSerialized;
    }

    public void setSensitivePayload(Map<String, String> sensitivePayload) {
        setSensitivePayloadSerialized(SerializationUtils.serializeToString(sensitivePayload));
    }

    public void setSensitivePayload(String key, String value) {
        Map<String, String> sensitivePayload = getSensitivePayload();
        sensitivePayload.put(key, value);
        setSensitivePayload(sensitivePayload);
    }

    public void setSensitivePayload(Field.Key key, String value) {
        setSensitivePayload(key.getFieldKey(), value);
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
                .add("id", getId())
                .add("userid", getUserId())
                .toString();
    }

    /**
     * Removes leading and trailing whitespace from whitelist of fields that we should trim
     */
    public void trimFields() {
        Map<String, String> fields = getFields();

        for (Field.Key key : TRIM_FIELDS_WHITELIST) {
            String fieldKey = key.getFieldKey();
            String field = fields.get(fieldKey);

            if (field != null) {
                fields.put(fieldKey, StringUtils.trim(field));
            }
        }

        setFields(fields);
    }

    /**
     * Check if this credential is a demo credential
     */
    @JsonIgnore
    public boolean isDemoCredentials() {
        for (DemoCredentials demoCredentials : DemoCredentials.values()) {
            final String demoUsername = demoCredentials.getUsername();
            if (fieldsSerialized != null && fieldsSerialized.contains(demoUsername)) {
                setUsername(demoUsername); // If demo-username is found on another field than username
                return true;
            }
        }

        return false;
    }

    @JsonIgnore
    public <T> T getPersistentSession(Class<T> returnType) {
        String payload = getSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME);

        if (Strings.isNullOrEmpty(payload)) {
            return null;
        }

        return SerializationUtils.deserializeFromString(payload, returnType);
    }

    @JsonIgnore
    public void setPersistentSession(Object object) {
        if (object == null) {
            removePersistentSession();
        }

        setSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME, SerializationUtils.serializeToString(object));
    }

    public void removePersistentSession() {
        removeSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME);
    }

    /**
     * Returns true if the credential is Mobile BankId and is successfully updated within 2 hours.
     * <p>
     * The reason to why there is a limit on 2 hours (KEEP_ALIVE_MAX_AGE) is because that the keep-alive logic is
     * one-directional. Calls are made to aggregation to keep credentials alive against bank or services but we don't
     * store if request was a success or failure. This check means that we not try to update credentials that was
     * updated for more than 2 hours ago since they most likely would not be alive any longer.
     * <p>
     * The check on 2 minutes (KEEP_ALIVE_MIN_AGE) is because it is not necessary to update the credentials directly
     * after it has been updated.
     */
    @JsonIgnore
    public boolean isPossibleToKeepAlive() {

        DateTime updateDateTime = new DateTime(updated);

        return type == CredentialsTypes.MOBILE_BANKID && status == CredentialsStatus.UPDATED &&
                updateDateTime.plus(KEEP_ALIVE_MAX_AGE).isAfterNow() &&
                updateDateTime.plus(KEEP_ALIVE_MIN_AGE).isBeforeNow();
    }

    public void addSensitivePayload(Map<String, String> payload) {
        for (String key : payload.keySet()) {
            setSensitivePayload(key, payload.get(key));
        }
    }
}
