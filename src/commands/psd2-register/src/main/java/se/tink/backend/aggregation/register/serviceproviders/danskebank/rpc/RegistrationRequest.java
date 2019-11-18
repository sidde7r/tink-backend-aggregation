package se.tink.backend.aggregation.register.serviceproviders.danskebank.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationRequest {

    @JsonProperty("token_endpoint_auth_method")
    private final String authMethod;

    @JsonProperty("grant_types")
    private final List<String> grantTypes;

    @JsonProperty("software_statement")
    private final String softwareStatement;

    @JsonProperty("id_token_signed_response_alg")
    private final String responseSigningAlg;

    @JsonProperty("request_object_signing_alg")
    private final String requestSigningAlg;

    @JsonProperty("tls_client_auth_dn")
    private final String tlsClientAuthDN;

    public RegistrationRequest(
            String authMethod,
            List<String> grantTypes,
            String tlsClientAuthDN,
            String softwareStatement) {
        this.authMethod = authMethod;
        this.grantTypes = grantTypes;
        this.softwareStatement = softwareStatement;
        this.tlsClientAuthDN = tlsClientAuthDN;
        this.responseSigningAlg = "PS256";
        this.requestSigningAlg = "PS256";
    }
}
