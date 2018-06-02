package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Table(value = "oauth2_client_events")
public class OAuth2ClientEvent {

    private static final TypeReference<HashMap<PayloadKey, String>> MAP_TYPE = new TypeReference<HashMap<PayloadKey, String>>() {
    };

    public enum Type {
        USER_REGISTERED,
        USER_AUTHORIZED
    }

    public enum PayloadKey {
        USERID
    }

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID clientId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID id;
    private String type;
    private String payload;
    private Date timestamp;
    @Transient
    @JsonIgnore
    private Map<PayloadKey, String> payloadMap;

    public OAuth2ClientEvent() {

    }

    public OAuth2ClientEvent(UUID oauth2ClientId, Type type, Map<PayloadKey, String> payload) {
        this.clientId = oauth2ClientId;
        this.id = UUIDs.timeBased();
        this.type = type.name();
        this.payload = SerializationUtils.serializeToString(payload);
        this.timestamp = new Date(UUIDs.unixTimestamp(this.id));
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Type getType() {
        return Type.valueOf(type);
    }

    public void setType(Type type) {
        this.type = type.name();
    }

    @Transient
    @JsonIgnore
    public Optional<String> getPayloadValue(PayloadKey key) {
        if (this.payloadMap == null) {
            this.payloadMap = SerializationUtils.deserializeFromString(this.payload, MAP_TYPE);
        }
        return Optional.ofNullable(this.payloadMap.get(key));
    }

    public static OAuth2ClientEvent createUserRegisteredEvent(String oauth2ClientId, String userId) {

        Map<PayloadKey, String> payload = Maps.newHashMap();
        payload.put(PayloadKey.USERID, userId);

        return new OAuth2ClientEvent(
                UUIDUtils.fromTinkUUID(oauth2ClientId),
                Type.USER_REGISTERED,
                payload);
    }

    public static OAuth2ClientEvent createUserAuthorizedEvent(String oauth2ClientId, String userId) {

        Map<PayloadKey, String> payload = Maps.newHashMap();
        payload.put(PayloadKey.USERID, userId);

        return new OAuth2ClientEvent(
                UUIDUtils.fromTinkUUID(oauth2ClientId),
                Type.USER_AUTHORIZED,
                payload);
    }
}
