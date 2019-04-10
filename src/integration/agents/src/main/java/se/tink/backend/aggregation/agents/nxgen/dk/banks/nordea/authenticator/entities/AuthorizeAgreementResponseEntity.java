package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementResponseEntity {
    private AuthenticationTokenEntity authenticationToken;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String secureMailAccess;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String nativeMessagingAccess;

    public AuthenticationTokenEntity getAuthenticationToken() {
        return authenticationToken;
    }

    public String getSecureMailAccess() {
        return secureMailAccess;
    }

    public String getNativeMessagingAccess() {
        return nativeMessagingAccess;
    }
}
