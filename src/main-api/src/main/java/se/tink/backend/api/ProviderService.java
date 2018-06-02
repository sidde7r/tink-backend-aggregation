package se.tink.backend.api;

import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Provider;
import se.tink.backend.rpc.ProviderListResponse;

public interface ProviderService {

    ProviderListResponse list(AuthenticatedUser user);

    ProviderListResponse list(AuthenticatedUser user, Provider.Capability providerCapability);

    ProviderListResponse list(String deviceToken, String market);

    ProviderListResponse listByMarket(AuthenticationContext authenticationContext, String oauth2ClientIdHeader,
            String market);

    ProviderListResponse suggest(AuthenticatedUser user);
}
