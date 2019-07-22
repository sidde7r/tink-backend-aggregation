package se.tink.backend.aggregation.register.nl.bunq;

import java.security.PublicKey;
import se.tink.backend.aggregation.register.nl.bunq.environment.sandbox.BunqRegisterSandboxUtils;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;

public final class SandboxEnvironment implements Environment {

    SandboxEnvironment() {}

    @Override
    public RegisterAsPSD2ProviderRequest createRegisterRequest(
            final PublicKey installationPublicKey, final String psd2ClientAuthToken) {
        return new RegisterAsPSD2ProviderRequest(
                BunqRegisterSandboxUtils.getQSealCCertificateAsString(),
                BunqRegisterSandboxUtils.getPaymentServiceProviderCertificateChainAsString(),
                BunqRegisterSandboxUtils.getClientPublicKeySignatureAsString(
                        installationPublicKey, psd2ClientAuthToken));
    }
}
