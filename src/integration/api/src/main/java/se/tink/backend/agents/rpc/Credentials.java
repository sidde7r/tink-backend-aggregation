package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.demo.DemoCredentials;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * This Credentials has been copied from {@link se.tink.libraries.credentials.rpc.Credentials} in an
 * effort to remove :aggregation-apis dependency on :main-api
 *
 * <p>Some of the objects here are not used by Aggregation at all, but are still needed until the
 * Aggregation API has been reworked. This is because users of the Aggregation API currently expects
 * to get the same data back as it sends away in the "enrichment" process.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credentials implements Cloneable {
    private static class FieldsMap extends HashMap<String, String> {}

    private Date debugUntil;
    private long providerLatency;

    @JsonIgnore private String fieldsSerialized;

    private String id;
    private Date nextUpdate;
    private String payload;
    private String providerName;
    private Date sessionExpiryDate;
    private CredentialsStatus status;
    private String statusPayload;
    private String statusPrompt;
    private Date statusUpdated;
    private String supplementalInformation;
    private CredentialsTypes type;
    private Date updated;
    private String userId;

    @JsonIgnore // Shoudn't be used between containers.
    private String sensitivePayloadSerialized;

    // Cross-cluster sensitive data.
    private String sensitiveDataSerialized;

    private void generateIdIfMissing() {
        if (id == null) {
            id = UUIDUtils.generateUUID();
        }
    }

    public void addSerializedFields(String maskedFields) {
        Map<String, String> fields = getFields();
        Map<String, String> newFields =
                SerializationUtils.deserializeFromString(maskedFields, FieldsMap.class);
        fields.putAll(newFields);
        setFields(fields);
    }

    /**
     * Removes any information that is not to be stored in the main database.
     *
     * @param provider
     */
    public void clearSensitiveInformation(Provider provider) {
        setSensitivePayloadAsMap(null);
        setFields(separateFields(provider, false));
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

    public boolean hasField(Field.Key field) {
        Map<String, String> fields = getFields();

        if (fields == null) {
            return false;
        }

        return fields.containsKey(field.getFieldKey());
    }

    public String getField(Field.Key field) {
        return getField(field.getFieldKey());
    }

    @JsonIgnore
    public Optional<String> getOptionalField(Field.Key field) {
        return Optional.ofNullable(getField(field.getFieldKey()));
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

    public Date getUpdated() {
        return updated;
    }

    public Date getSessionExpiryDate() {
        return sessionExpiryDate;
    }

    public String getSensitivePayloadSerialized() {
        return sensitivePayloadSerialized;
    }

    public Map<String, String> getSensitivePayloadAsMap() {
        if (Strings.isNullOrEmpty(getSensitivePayloadSerialized())) {
            return Maps.newHashMap();
        }

        Map<String, String> sensitivePayload =
                SerializationUtils.deserializeFromString(
                        getSensitivePayloadSerialized(),
                        new TypeReference<HashMap<String, String>>() {});

        // `sensitivePayload` is `null` if we're unable to deserialize the payload
        if (sensitivePayload == null) {
            sensitivePayload = Maps.newHashMap();
        }

        return sensitivePayload;
    }

    public String getSensitivePayload(String key) {
        if (getSensitivePayloadAsMap() == null) {
            return null;
        }

        return getSensitivePayloadAsMap().get(key);
    }

    public Optional<String> getSensitivePayload(Field.Key key) {
        return Optional.ofNullable(Strings.emptyToNull(getSensitivePayload(key.getFieldKey())));
    }

    public <T> Optional<T> getSensitivePayload(Field.Key key, Class<T> cls) {
        Optional<String> data = getSensitivePayload(key);
        return data.map(d -> SerializationUtils.deserializeFromString(d, cls));
    }

    public <T> Optional<T> getSensitivePayload(Field.Key key, TypeReference<T> typeReference) {
        Optional<String> data = getSensitivePayload(key);
        return data.map(d -> SerializationUtils.deserializeFromString(d, typeReference));
    }

    public void removeSensitivePayload(Field.Key key) {
        Map<String, String> sensitivePayload = getSensitivePayloadAsMap();

        if (sensitivePayload == null) {
            return;
        }

        sensitivePayload.remove(key.getFieldKey());

        setSensitivePayloadAsMap(sensitivePayload);
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

    public String getUserId() {
        return this.userId;
    }

    @Deprecated
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

    private Map<String, String> separateFields(Provider provider, boolean sensitive) {
        Map<String, String> fields = Maps.newHashMap();

        Set<Entry<String, String>> credentialsFields = getFields().entrySet();
        List<Field> providerFields = provider.getFields();

        for (final Entry<String, String> fieldEntry : credentialsFields) {
            Field field =
                    providerFields.stream()
                            .filter(f -> Objects.equal(fieldEntry.getKey(), f.getName()))
                            .findFirst()
                            .orElse(null);

            if (field != null && sensitive == (field.isSensitive() | field.isMasked())) {
                // Fields exists and has same config of sensitive as asked for
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

    @JsonProperty
    public void setSessionExpiryDate(Date sessionExpiryDate) {
        this.sessionExpiryDate = sessionExpiryDate;
    }

    @JsonIgnore
    public void setSessionExpiryDate(LocalDate sessionExpiryDate) {
        this.sessionExpiryDate =
                Date.from(
                        sessionExpiryDate
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
    }

    public void setSensitivePayloadSerialized(String sensitivePayloadSerialized) {
        this.sensitivePayloadSerialized = sensitivePayloadSerialized;
    }

    public void setSensitivePayloadAsMap(Map<String, String> sensitivePayload) {
        setSensitivePayloadSerialized(SerializationUtils.serializeToString(sensitivePayload));
    }

    public void setSensitivePayload(String key, String value) {
        Map<String, String> sensitivePayload = getSensitivePayloadAsMap();
        sensitivePayload.put(key, value);
        setSensitivePayloadAsMap(sensitivePayload);
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

    /** Check if this credential is a demo credential */
    @JsonIgnore
    public boolean isDemoCredentials() {

        if (fieldsSerialized == null) {
            return false;
        }

        for (DemoCredentials demoCredentials : DemoCredentials.values()) {
            final String demoUsername = demoCredentials.getUsername();
            if (fieldsSerialized.contains(demoUsername)) {
                setUsername(
                        demoUsername); // If demo-username is found on another field than username
                return true;
            }
        }

        return false;
    }

    @JsonIgnore
    public <T> T getPersistentSession(Class<T> returnType) {
        Optional<String> payload = getSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME);
        return payload.map(p -> SerializationUtils.deserializeFromString(p, returnType))
                .orElse(null);
    }

    @JsonIgnore
    public void setPersistentSession(Object object) {
        if (object == null) {
            removePersistentSession();
        }

        setSensitivePayload(
                Field.Key.PERSISTENT_LOGIN_SESSION_NAME,
                SerializationUtils.serializeToString(object));
    }

    public void removePersistentSession() {
        removeSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME);
    }

    public void addSensitivePayload(Map<String, String> payload) {
        for (String key : payload.keySet()) {
            setSensitivePayload(key, payload.get(key));
        }
    }

    @JsonIgnore
    public String getMetricTypeName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, getType().name());
    }
}
