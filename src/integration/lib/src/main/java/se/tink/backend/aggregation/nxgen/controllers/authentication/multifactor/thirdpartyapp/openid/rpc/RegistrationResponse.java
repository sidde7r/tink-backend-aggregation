package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationResponse {
    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    private List<String> scopes;

    @JsonProperty("token_endpoint_auth_method")
    private List<String> tokenEndpointAuthMethod;

    @JsonProperty("token_endpoint_auth_signing_alg")
    private List<String> tokenEndpointAuthSigningAlg;

    @JsonProperty("id_token_signed_response_alg")
    private String idTokenSignedResponseAlg;

    @JsonProperty("request_object_signing_alg")
    private List<String> requestObjectSigningAlg;

    private String certDn;

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("client_id_issued_at")
    private long clientIdIssuedAt;

    @JsonProperty("client_secret_expires_at")
    private int clientSecretExpiresAt;

    @JsonProperty("_id")
    private String id;

    @Override
    public String toString() {
        ObjectMapper m = new ObjectMapper();
        try {
            return m.writerWithDefaultPrettyPrinter().writeValueAsString(this);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
