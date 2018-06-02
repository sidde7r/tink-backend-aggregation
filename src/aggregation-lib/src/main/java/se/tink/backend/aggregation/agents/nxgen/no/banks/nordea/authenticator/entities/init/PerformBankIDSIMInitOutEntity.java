package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.init;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.AuthenticationTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PerformBankIDSIMInitOutEntity {
    private AuthenticationTokenEntity authenticationToken;

    private Map<String, Object> sessionId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String merchantReference;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cancelUrl;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String verifyUrl;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String callbackUrl;

    public AuthenticationTokenEntity getAuthenticationToken() {
        return authenticationToken;
    }

    public String getSessionId() {
        return (String) ((Map) sessionId.get("@id")).get("$");
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
