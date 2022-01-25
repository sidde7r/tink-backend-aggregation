package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Builder
@RequiredArgsConstructor
public class DkSSAuthenticatorsConfig {

    @Singular("addProvider")
    private final Map<DkSSMethod, DkSSAuthenticatorProvider> authenticationProviders;

    public Optional<DkSSAuthenticatorProvider> getAuthenticationProviderForMethod(
            DkSSMethod method) {
        return Optional.ofNullable(authenticationProviders.get(method));
    }
}
