package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationTokenEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String token;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String authLevel;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String remeberUserId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String authMethod;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String loginTime;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String notAfter;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String sessionMaxLength;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String tokenMaxAge;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String swippExists;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String profileId;

    public String getToken() {
        return token;
    }

    public String getAuthLevel() {
        return authLevel;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public String getSessionMaxLength() {
        return sessionMaxLength;
    }

    public String getTokenMaxAge() {
        return tokenMaxAge;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getRemeberUserId() {
        return remeberUserId;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getSwippExists() {
        return swippExists;
    }
}
