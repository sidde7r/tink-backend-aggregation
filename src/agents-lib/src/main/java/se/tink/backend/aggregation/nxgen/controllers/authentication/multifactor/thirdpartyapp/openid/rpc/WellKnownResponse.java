package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

// According to "OpenID Connect Discovery 1.0"

@JsonObject
public class WellKnownResponse {
    private String version;
    private String issuer;
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;
    @JsonProperty("token_endpoint")
    private String tokenEndpoint;
    @JsonProperty("revocation_endpoint")
    private String revocationEndpoint;
    @JsonProperty("jwks_uri")
    private String jwksUri;
    @JsonProperty("registration_endpoint")
    private String registrationEndpoint;
    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;
    @JsonProperty("claims_supported")
    private List<String> claimsSupported;
    @JsonProperty("acr_values_supported")
    private List<String> acrValuesSupported;
    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported;
    @JsonProperty("response_modes_supported")
    private List<String> responseModesSupported;
    @JsonProperty("grant_types_supported")
    private List<String> grantTypesSupported;
    @JsonProperty("subject_types_supported")
    private List<String> subjectTypesSupported;
    @JsonProperty("id_token_signing_alg_values_supported")
    private Set<String> idTokenSigningAlgValuesSupported;
    @JsonProperty("token_endpoint_auth_methods_supported")
    private Set<String> tokenEndpointAuthMethodsSupported;
    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
    private Set<String> tokenEndpointAuthSigningAlgValuesSupported;
    @JsonProperty("claim_types_supported")
    private Set<String> claimTypesSupported;
    @JsonProperty("claims_parameter_supported")
    private boolean claimsParameterSupported;
    @JsonProperty("request_parameter_supported")
    private boolean requestParameterSupported;
    @JsonProperty("request_uri_parameter_supported")
    private boolean requestUriParameterSupported;
    @JsonProperty("request_object_signing_alg_values_supported")
    private Set<String> requestObjectSigningAlgValuesSupported;
    @JsonProperty("request_object_encryption_alg_values_supported")
    private Set<String> requestObjectEncryptionAlgValuesSupported;
    @JsonProperty("request_object_encryption_enc_values_supported")
    private Set<String> requestObjectEncryptionEncValuesSupported;

    public String getVersion() {
        return version;
    }

    public String getIssuer() {
        return issuer;
    }

    public URL getAuthorizationEndpoint() {
        return new URL(authorizationEndpoint);
    }

    public URL getTokenEndpoint() {
        return new URL(tokenEndpoint);
    }

    public URL getRevocationEndpoint() {
        return new URL("");
    }

    public URL getRegistrationEndpoint() {
        return new URL(registrationEndpoint);
    }

    public URL getJwksUri() {
        return new URL(jwksUri);
    }

    public boolean hasResponseTypes(List<String> responseTypes) {
        // responseTypesSupported will contain, for example: <"code", "code id_token">
        // We want to make sure that it contains any variation of, for example: "code" and "id_token", regardless
        // of which order they are specified ("id_token code" <> "code id_token").
        return responseTypesSupported.stream()
                .map(s -> Arrays.asList(s.split(" ")))
                .anyMatch(supportedTypes -> supportedTypes.containsAll(responseTypes));
    }

    public List<String> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public boolean hasGrantTypes(List<String> grantTypes) {
        return grantTypesSupported.containsAll(grantTypes);
    }

    public List<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public Optional<String> verifyAndGetScopes(List<String> scopes) {
        if (!scopesSupported.containsAll(scopes)) {
            return Optional.empty();
        }

        return Optional.of(scopes.stream().collect(Collectors.joining(" ")));
    }

    public Optional<String> vertfyAndGetEnpointAuthMethod(String authMethod) {
        if (!tokenEndpointAuthMethodsSupported.contains(authMethod)) {
            return Optional.empty();
        }

        return Optional.of(authMethod);
    }

    public Optional<String> getPreferredIdTokenSigningAlg(List<String> supportedAlgs) {
        return supportedAlgs.stream()
                .filter(alg -> idTokenSigningAlgValuesSupported.contains(alg))
                .findFirst();
    }

    public Optional<String> getPreferredTokenEndpointSigningAlg(List<String> supportedAlgs) {
        return supportedAlgs.stream()
                .filter(alg -> tokenEndpointAuthSigningAlgValuesSupported.contains(alg))
                .findFirst();
    }

    public Optional<String> getPreferredRequestObjectSigningAlg(List<String> supportedAlgs) {
        return supportedAlgs.stream()
                .filter(alg -> requestObjectSigningAlgValuesSupported.contains(alg))
                .findFirst();
    }

    public Optional<OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD> getPreferredTokenEndpointAuthMethod(
            List<OpenIdConstants.TOKEN_ENDPOINT_AUTH_METHOD> supportedMethods) {
        return supportedMethods.stream()
                .filter(method -> tokenEndpointAuthMethodsSupported.contains(method.toString()))
                .findFirst();
    }

    public boolean isRequestParameterSupported() {
        return requestParameterSupported;
    }

}
