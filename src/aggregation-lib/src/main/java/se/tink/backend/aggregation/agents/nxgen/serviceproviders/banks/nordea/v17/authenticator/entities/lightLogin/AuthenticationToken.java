package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator.entities.lightLogin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationToken {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String token;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String authLevel;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date loginTime;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date notAfter;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Integer.class)
    private Integer sessionMaxLength;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Integer.class)
    private Integer tokenMaxAge;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String profileId;

    public String getToken() {
        return token;
    }

    public String getAuthLevel() {
        return authLevel;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public Integer getSessionMaxLength() {
        return sessionMaxLength;
    }

    public Integer getTokenMaxAge() {
        return tokenMaxAge;
    }

    public String getProfileId() {
        return profileId;
    }
}
