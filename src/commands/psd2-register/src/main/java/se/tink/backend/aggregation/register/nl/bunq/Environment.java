package se.tink.backend.aggregation.register.nl.bunq;

import java.security.PublicKey;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;

@FunctionalInterface
interface Environment {
    RegisterAsPSD2ProviderRequest createRegisterRequest(
            PublicKey installationPublicKey, String psd2ClientAuthToken);
}
