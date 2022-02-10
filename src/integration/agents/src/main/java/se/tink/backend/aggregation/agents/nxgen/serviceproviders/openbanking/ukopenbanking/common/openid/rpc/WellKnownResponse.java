package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.TokenEndpointAuthMethod;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
public class WellKnownResponse {

    @Getter private String version;

    @Getter private String issuer;

    private String authorizationEndpoint;

    private String tokenEndpoint;

    private String jwksUri;

    private List<String> scopesSupported;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> responseTypesSupported;

    private Set<String> idTokenSigningAlgValuesSupported;

    private Set<String> tokenEndpointAuthMethodsSupported;

    public URL getAuthorizationEndpoint() {
        return new URL(authorizationEndpoint);
    }

    public URL getTokenEndpoint() {
        return new URL(tokenEndpoint);
    }

    public URL getJwksUri() {
        return new URL(jwksUri);
    }

    public List<String> getScopesSupported() {
        return scopesSupported;
    }

    public String getResponseType() {
        if (responseTypesSupported.contains(
                parseResponseType(
                        OpenIdConstants.RESPONSE_TYPES_FOR_HYBRID_FLOW_WITH_ID_TOKEN_AND_TOKEN))) {
            return parseResponseType(
                    OpenIdConstants.RESPONSE_TYPES_FOR_HYBRID_FLOW_WITH_ID_TOKEN_AND_TOKEN);
        }
        return parseResponseType(OpenIdConstants.RESPONSE_TYPES_FOR_HYBRID_FLOW_WITH_ID_TOKEN);
    }

    private String parseResponseType(List<String> responseTypes) {
        return String.join(" ", responseTypes);
    }

    public Boolean isOfflineAccessSupported() {
        return scopesSupported.contains(Scopes.OFFLINE_ACCESS);
    }

    public Optional<String> verifyAndGetScopes(List<String> scopes) {
        if (!scopesSupported.containsAll(scopes)) {
            return Optional.empty();
        }

        return Optional.of(String.join(" ", scopes));
    }

    public Optional<SigningAlgorithm> getPreferredIdTokenSigningAlg(
            List<SigningAlgorithm> supportedAlgs) {
        return supportedAlgs.stream()
                .filter(alg -> idTokenSigningAlgValuesSupported.contains(alg.name()))
                .findFirst();
    }

    public Optional<TokenEndpointAuthMethod> getPreferredTokenEndpointAuthMethod(
            List<TokenEndpointAuthMethod> supportedMethods) {
        return supportedMethods.stream()
                .filter(
                        method ->
                                tokenEndpointAuthMethodsSupported.contains(
                                        method.toString().toLowerCase()))
                .findFirst();
    }
}
