package se.tink.backend.core.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "oauth2_webhooks")
public class OAuth2WebHook {
    private static final TypeReference<Set<String>> SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };

    @Id
    private String id;
    private String userId;
    private String secret;
    private String url;
    @Column(name = "`events`")
    @Type(type = "text")
    private String eventsSerialized;
    private String clientId;
    private boolean global; // If this is true, userId can be null and the webhook concerns all users.

    public OAuth2WebHook() {
        id = StringUtils.generateUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Transient
    @JsonProperty(value = "events")
    public Set<String> getEvents() {
        return SerializationUtils.deserializeFromString(this.eventsSerialized, SET_TYPE_REFERENCE);
    }

    public void setEvents(Set<String> events) {
        this.eventsSerialized = SerializationUtils.serializeToString(events);
    }

    @JsonIgnore
    public String getEventsSerialized() {
        return eventsSerialized;
    }

    @JsonIgnore
    public void setEventsSerialized(String eventsSerialized) {
        this.eventsSerialized = eventsSerialized;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
