package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.TokenEndpointAuthMethod;
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

    public Optional<String> verifyAndGetScopes(List<String> scopes) {
        if (!scopesSupported.containsAll(scopes)) {
            return Optional.empty();
        }

        return Optional.of(String.join(" ", scopes));
    }

    public Optional<String> getPreferredIdTokenSigningAlg(List<String> supportedAlgs) {
        return supportedAlgs.stream()
                .filter(alg -> idTokenSigningAlgValuesSupported.contains(alg))
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
