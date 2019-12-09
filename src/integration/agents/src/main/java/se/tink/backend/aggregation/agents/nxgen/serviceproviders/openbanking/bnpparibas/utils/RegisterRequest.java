package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterRequest {

    @JsonProperty("redirect_uris")
    private final List<String> redirectUris;

    @JsonProperty("token_endpoint_auth_method")
    private final String tokenEndpointAuthMethod;

    @JsonProperty("grant_types")
    private final List<String> grantTypes;

    @JsonProperty("client_name")
    private final String clientName;

    @JsonProperty("provider_legal_id")
    private final String providerLegalId;

    private final List<String> contacts;

    private final String context;

    private final String scope;

    public RegisterRequest(
            List<String> redirectUris,
            String token_endpoint_auth_method,
            List<String> grantTypes,
            String clientName,
            List<String> contacts,
            String providerLegalId,
            String context,
            String scopes) {
        this.redirectUris = redirectUris;
        this.tokenEndpointAuthMethod = token_endpoint_auth_method;
        this.grantTypes = grantTypes;
        this.clientName = clientName;
        this.contacts = contacts;
        this.providerLegalId = providerLegalId;
        this.context = context;
        this.scope = scopes;
    }
}
