package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "users_devices")
public class UserDevice {
    private static final TypeReference<? extends Map<PayloadKey, String>> PAYLOAD_TYPE_REFERENCE = new TypeReference<Map<PayloadKey, String>>() {};

    @Tag(1)
    private String deviceId;

    @Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Tag(3)
    private Date inserted;

    @Tag(4)
    @Enumerated(EnumType.STRING)
    private UserDeviceStatuses status;

    @Tag(5)
    private Date updated;

    @Tag(6)
    private String userAgent;

    @Tag(7)
    private String userId;

    @Column(name = "`payload`")
    @Type(type = "text")
    @Exclude
    @JsonIgnore
    private String payloadSerialized;

    public String getDeviceId() {
        return deviceId;
    }

    public int getId() {
        return id;
    }

    public Date getInserted() {
        return inserted;
    }

    public UserDeviceStatuses getStatus() {
        return status;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getUserId() {
        return userId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }

    public void setStatus(UserDeviceStatuses status) {
        this.status = status;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean updateIfNeeded(String userAgent) {
        if (this.userAgent == null || !this.userAgent.equals(userAgent)) {
            this.userAgent = userAgent;
            return true;
        }
        return false;
    }

    public Map<PayloadKey, String> getPayload() {
        Map<PayloadKey, String> payload = getPayloadRaw();

        // Mask the SSN before we sent it to the clients.

        if (payload.containsKey(PayloadKey.SWEDISH_SSN)) {
            payload.put(PayloadKey.SWEDISH_SSN, StringUtils.maskSSN(payload.get(PayloadKey.SWEDISH_SSN)));
        }

        return payload;
    }

    @JsonIgnore
    public Map<PayloadKey, String> getPayloadRaw() {
        if (Strings.isNullOrEmpty(payloadSerialized)) {
            return Maps.newHashMap();
        }

        return SerializationUtils.deserializeFromString(payloadSerialized, PAYLOAD_TYPE_REFERENCE);
    }

    @JsonIgnore
    public Optional<String> getPayloadRaw(PayloadKey key) {
        return Optional.ofNullable(getPayloadRaw().get(key));
    }

    public void addPayload(PayloadKey key, String value) {
        Map<PayloadKey, String> payload;

        if (Strings.isNullOrEmpty(payloadSerialized)) {
            payload = Maps.newHashMap();
        } else {
            payload = getPayload();
        }

        payload.put(key, value);

        payloadSerialized = SerializationUtils.serializeToString(payload);
    }

    public void removePayload(PayloadKey key) {
        Map<PayloadKey, String> payload = getPayload();
        payload.remove(key);

        if (payload.isEmpty()) {
            payloadSerialized = null;
        } else {
            payloadSerialized = SerializationUtils.serializeToString(payloadSerialized);
        }
    }

    public enum PayloadKey {
        DEMO_SSN, SWEDISH_SSN
    }
}
