package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.SignatureKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.TransportKey;

@JsonObject
public class SoftwareStatement {

    private SoftwareStatementAssertion softwareStatementAssertion;
    private SignatureKey signingKey;
    private TransportKey transportKey;

    private Map<String, ProviderConfiguration> providerConfigurations;

    public String getSigningKeyId() {
        return signingKey.getKeyId();
    }

    public RSAPrivateKey getSigningKey() {
        return signingKey.getRSAPrivateKey();
    }

    public byte[] getTransportKeyP12() {
        return transportKey.getP12Key();
    }

    public String getTransportKeyPassword() {
        return transportKey.getPassword();
    }

    public String getAssertion() {
        return softwareStatementAssertion.getAssertion();
    }

    public String getSoftwareId() {
        return softwareStatementAssertion.getSoftwareId();
    }

    public String[] getRedirectUris() {
        return softwareStatementAssertion.getRedirectUris();
    }

    public ProviderConfiguration getProviderConfiguration(String name) {
        return providerConfigurations.get(name);
    }
}
