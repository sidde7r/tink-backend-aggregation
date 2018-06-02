package se.tink.backend.core.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
@Table(name = "oauth2_clients")
public class OAuth2Client {

    public static class PayloadKey {
        public static final String PROVIDERS = "PROVIDERS";
        public static final String CUSTOM_CSS = "CUSTOM_CSS";
        public static final String DESTINATION_ACCOUNT_PATTERN = "DESTINATION_ACCOUNT_PATTERN";
        public static final String REFRESHABLE_ITEMS = "REFRESHABLE_ITEMS";
        public static final String AUTO_AUTHORIZE = "AUTO_AUTHORIZE";
        public static final String ALLOW_DEMO_CREDENTIALS = "ALLOW_DEMO_CREDENTIALS";
        public static final String DOESNT_PRODUCE_TINK_USERS = "DOESNT_PRODUCE_TINK_USERS";
        public static final String WEBHOOK_DOMAINS = "WEBHOOK_DOMAINS";
    }

    private static final TypeReference<HashMap<String, String>> STRING_MAP_TYPE = new TypeReference<HashMap<String, String>>() {
    };
    
    private static final TypeReference<Set<String>> STRING_SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };

    private String name;
    private String id;
    private Set<String> redirectUris;
    private String redirectUrisSerialized;
    private String scope;
    private String secret;
    private String url;
    private String iconUrl;
    private String payloadSerialized;
    private boolean embeddedAllowed;

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public OAuth2Client() {
        id = StringUtils.generateUUID();
        secret = StringUtils.generateUUID();
    }

    public String getName() {
        return name;
    }

    @Id
    public String getId() {
        return id;
    }

    @Transient
    public Set<String> getRedirectUris() {
        if (!Strings.isNullOrEmpty(redirectUrisSerialized)) {
            return SerializationUtils.deserializeFromString(redirectUrisSerialized, STRING_SET_TYPE_REFERENCE);
        }
        return null;
    }

    @JsonIgnore
    @Column(name = "`redirecturis`")
    @Type(type = "text")
    public String getRedirectUrisSerialized() {
        if (redirectUris != null) {
            return SerializationUtils.serializeToString(redirectUris);
        } else {
            return redirectUrisSerialized;
        }
    }

    @JsonIgnore
    @Column(name = "`payload`")
    @Type(type = "text")
    public String getPayloadSerialized() {
        return payloadSerialized;
    }

    public void setPayloadSerialized(String payloadSerialized) {
        this.payloadSerialized = payloadSerialized;
    }

    public String getScope() {
        return scope;
    }

    public String getSecret() {
        return secret;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        if (redirectUris == null) {
            return;
        }

        this.redirectUris = redirectUris;

        if (redirectUris != null) {
            redirectUrisSerialized = SerializationUtils.serializeToString(redirectUris);
        }
    }

    public void setRedirectUrisSerialized(String redirectUrisSerialized) {
        if (Strings.isNullOrEmpty(redirectUrisSerialized)) {
            return;
        }

        this.redirectUrisSerialized = redirectUrisSerialized;

        if (!Strings.isNullOrEmpty(redirectUrisSerialized)) {
            redirectUris = SerializationUtils.deserializeFromString(redirectUrisSerialized, STRING_SET_TYPE_REFERENCE);
        }
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    @Transient
    public OAuth2ClientScopes getOAuth2Scope() {
        return new OAuth2ClientScopes(this.getScope());
    }

    @JsonIgnore
    @Transient
    public Map<String, String> getPayload() {
        if (Strings.isNullOrEmpty(payloadSerialized)) {
            return Maps.newHashMap();
        }
        return SerializationUtils.deserializeFromString(payloadSerialized, STRING_MAP_TYPE);
    }

    @JsonIgnore
    @Transient
    public Optional<String> getPayloadValue(String key) {
        Map<String, String> map = getPayload();
        return Optional.ofNullable(map.get(key));
    }

    @JsonIgnore
    @Column(columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    public boolean getEmbeddedAllowed() {
        return embeddedAllowed;
    }

    public void setEmbeddedAllowed(boolean embeddedAllowed) {
        this.embeddedAllowed = embeddedAllowed;
    }

    @JsonIgnore
    @Transient
    public boolean allowDemoCredentials() {
        Optional<String> allowDemoUsers = getPayloadValue(PayloadKey.ALLOW_DEMO_CREDENTIALS);
        if (!allowDemoUsers.isPresent()) {
            return false;
        }

        return "true".equals(allowDemoUsers.get());
    }

    @JsonIgnore
    @Transient
    public boolean hasAutoAuthorize() {
        Optional<String> autoAuthorize = getPayloadValue(PayloadKey.AUTO_AUTHORIZE);
        if (!autoAuthorize.isPresent()) {
            return false;
        }

        return "true".equals(autoAuthorize.get());
    }

    /**
     * We are selling b2b services, and in agreements with some partners, it's stated that we do not own the user.
     * The user never signs our TOC. This also means that we need to clear these users a few hours after providing
     * the service (e.g. account information or payment initiation).
     *
     */
    @JsonIgnore
    @Transient
    public boolean doesntProduceTinkUsers() {
        Optional<String> cleanUsers = getPayloadValue(PayloadKey.DOESNT_PRODUCE_TINK_USERS);
        if (!cleanUsers.isPresent()) {
            return false;
        }

        return "true".equals(cleanUsers.get());
    }
}
