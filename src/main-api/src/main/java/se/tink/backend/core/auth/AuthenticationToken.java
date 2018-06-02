package se.tink.backend.core.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken {
    private static final TypeReference<Map<AuthenticationPayloadKey, String>> PAYLOAD_MAP_TYPE_REFERENCE =
            new TypeReference<Map<AuthenticationPayloadKey, String>>() {
            };

    @Id
    private String token;
    private String userId;
    @Enumerated(EnumType.STRING)
    private AuthenticationMethod method;
    @Enumerated(EnumType.STRING)
    private AuthenticationStatus status;
    private Date created;
    private String clientKey;
    private String oAuth2ClientId;
    @Type(type = "text")
    private String payloadSerialized;
    @Transient
    private Map<AuthenticationPayloadKey, String> payload;

    public Date getCreated() {
        return created;
    }

    public String getToken() {
        return token;
    }

    public AuthenticationMethod getMethod() {
        return method;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getOAuth2ClientId() {
        return oAuth2ClientId;
    }

    public String getUserId() {
        return userId;
    }

    public Map<AuthenticationPayloadKey, String> getPayload() {
        if (payload == null) {
            deserializePayload();
        }
        return payload;
    }

    private void deserializePayload() {
        this.payload = payloadSerialized == null ?
                Maps.newHashMap() :
                SerializationUtils.deserializeFromString(payloadSerialized, PAYLOAD_MAP_TYPE_REFERENCE);
    }

    public String getPayloadMarket() {
        return getPayload().get(AuthenticationPayloadKey.MARKET);
    }

    public String getPayloadHashedPassword() {
        return getPayload().get(AuthenticationPayloadKey.HASHED_PASSWORD);
    }

    public String getPayloadNationalId() {
        return getPayload().get(AuthenticationPayloadKey.NATIONAL_ID);
    }

    public String getPayloadUsername() {
        return getPayload().get(AuthenticationPayloadKey.USERNAME);
    }

    public String getPayloadAuthenticatedDeviceId() {
        return getPayload().get(AuthenticationPayloadKey.AUTHENTICATED_DEVICE_ID);
    }

    public static AuthenticationTokenBuilder builder() {
        return new AuthenticationTokenBuilder();
    }

    public final static class AuthenticationTokenBuilder {
        private AuthenticationMethod method;
        private AuthenticationStatus status;
        private String clientKey;
        private String oAuth2ClientId;
        private String userId;
        private String token;
        private String nationalId;
        private String hashedPassword;
        private String username;
        private String marketCode;
        private String authenticatedDeviceId;

        public AuthenticationTokenBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        public AuthenticationTokenBuilder withMethod(AuthenticationMethod method) {
            this.method = method;
            return this;
        }

        public AuthenticationTokenBuilder withClientKey(String clientKey) {
            this.clientKey = clientKey;
            return this;
        }

        public AuthenticationTokenBuilder withOAuth2ClientId(String oAuth2ClientId) {
            this.oAuth2ClientId = oAuth2ClientId;
            return this;
        }

        public AuthenticationTokenBuilder withStatus(AuthenticationStatus status) {
            this.status = status;
            return this;
        }

        public AuthenticationTokenBuilder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public AuthenticationTokenBuilder withNationalId(String nationalId) {
            this.nationalId = nationalId;
            return this;
        }

        public AuthenticationTokenBuilder withHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
            return this;
        }

        public AuthenticationTokenBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public AuthenticationTokenBuilder withMarket(String marketCode) {
            this.marketCode = marketCode;
            return this;
        }

         public AuthenticationTokenBuilder withAuthenticatedDeviceId(String authenticatedDeviceId) {
            this.authenticatedDeviceId = authenticatedDeviceId;
            return this;
        }

        public AuthenticationToken build() {
            Preconditions.checkNotNull(method, "Method must not be null.");
            Preconditions.checkNotNull(status, "Status must not be null.");

            if (status == AuthenticationStatus.AUTHENTICATED) {
                Preconditions.checkNotNull(userId, "UserId must not be null if status is 'AUTHENTICATED'.");
            }

            if (status == AuthenticationStatus.NO_USER) {
                Preconditions.checkState(Strings.isNullOrEmpty(userId), "UserId must be null if status is 'NO_USER'.");
            }

            AuthenticationToken authenticationToken = new AuthenticationToken();

            // Either generate a token or use the one specified
            if (Strings.isNullOrEmpty(token)) {
                authenticationToken.token = UUID.randomUUID().toString();
            } else {
                authenticationToken.token = token;
            }

            // Assign the properties
            authenticationToken.method = method;
            authenticationToken.status = status;
            authenticationToken.clientKey = clientKey;
            authenticationToken.oAuth2ClientId = oAuth2ClientId;
            authenticationToken.created = new Date();
            authenticationToken.userId = userId;

            // Create the payload
            Map<AuthenticationPayloadKey, String> payload = Maps.newHashMap();

            if (!Strings.isNullOrEmpty(nationalId)) {
                payload.put(AuthenticationPayloadKey.NATIONAL_ID, nationalId);
            }

            if (!Strings.isNullOrEmpty(hashedPassword)) {
                payload.put(AuthenticationPayloadKey.HASHED_PASSWORD, hashedPassword);
            }

            if (!Strings.isNullOrEmpty(username)) {
                payload.put(AuthenticationPayloadKey.USERNAME, username);
            }

            if (!Strings.isNullOrEmpty(marketCode)) {
                payload.put(AuthenticationPayloadKey.MARKET, marketCode);
            }

            if (!Strings.isNullOrEmpty(authenticatedDeviceId)) {
                payload.put(AuthenticationPayloadKey.AUTHENTICATED_DEVICE_ID, authenticatedDeviceId);
            }

            if (payload.isEmpty()) {
                authenticationToken.payload = null;
                authenticationToken.payloadSerialized = null;
            } else {
                authenticationToken.payload = payload;
                authenticationToken.payloadSerialized = SerializationUtils.serializeToString(payload);
            }

            return authenticationToken;
        }
    }
}

