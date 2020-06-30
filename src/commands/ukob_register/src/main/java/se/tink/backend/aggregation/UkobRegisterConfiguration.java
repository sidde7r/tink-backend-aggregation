package se.tink.backend.aggregation;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SignatureKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.TransportKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.LocalKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.LocalCertificateTlsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

@JsonObject
public class UkobRegisterConfiguration implements UkOpenBankingClientConfigurationAdapter {

    private SoftwareStatement softwareStatement;
    private String rootCAData;
    private String rootCAPassword;
    private SignatureKey signingKey;
    private TransportKey transportKey;

    public SoftwareStatement getSoftwareStatement() {
        return softwareStatement;
    }

    public byte[] getRootCAData() {
        return EncodingUtils.decodeBase64String(rootCAData);
    }

    public String getRootCAPassword() {
        return rootCAPassword;
    }

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        return null;
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return softwareStatement.getAssertion();
    }

    @Override
    public Optional<TlsConfigurationOverride> getTlsConfigurationOverride() {
        return Optional.of(new LocalCertificateTlsConfiguration(transportKey));
    }

    @Override
    public Optional<JwtSigner> getSignerOverride() {
        return Optional.of(
                new LocalKeySigner(signingKey.getKeyId(), signingKey.getRSAPrivateKey()));
    }
}
