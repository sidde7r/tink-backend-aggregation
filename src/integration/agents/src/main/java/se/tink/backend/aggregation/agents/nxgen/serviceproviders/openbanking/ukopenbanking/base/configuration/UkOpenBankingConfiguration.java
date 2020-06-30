package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.LocalKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.LocalCertificateTlsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

@JsonObject
public class UkOpenBankingConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty @Secret private String organizationId;
    @JsonProperty @SensitiveSecret @ClientIdConfiguration private String clientId;
    @JsonProperty @Secret private String signingKey;
    @JsonProperty @Secret private String signingKeyId;
    @JsonProperty @Secret private String softwareStatementAssertion;
    @JsonProperty @Secret private String softwareId;
    @JsonProperty @Secret private String transportKey;
    @JsonProperty @Secret private String transportKeyId;
    @JsonProperty @Secret private String tokenEndpointAuthSigningAlg;
    @JsonProperty @Secret private String tokenEndpointAuthMethod;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @JsonProperty @SensitiveSecret private String transportKeyPassword;
    @JsonProperty @SensitiveSecret private String signingKeyPassword;

    public String getOrganizationId() {
        return organizationId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public String getSigningKeyId() {
        return signingKeyId;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public String getTransportKey() {
        return transportKey;
    }

    public String getTransportKeyId() {
        return transportKeyId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTransportKeyPassword() {
        return transportKeyPassword;
    }

    public String getSigningKeyPassword() {
        return signingKeyPassword;
    }

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        return new ProviderConfiguration(
                organizationId,
                new ClientInfo(
                        clientId,
                        clientSecret,
                        tokenEndpointAuthMethod,
                        tokenEndpointAuthSigningAlg));
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return new SoftwareStatementAssertion(softwareStatementAssertion, softwareId);
    }

    @Override
    public Optional<TlsConfigurationOverride> getTlsConfigurationOverride() {
        return Optional.of(
                new LocalCertificateTlsConfiguration(
                        transportKeyId, transportKey, transportKeyPassword));
    }

    @Override
    public Optional<JwtSigner> getSignerOverride() {
        return Optional.of(
                new LocalKeySigner(
                        signingKeyId,
                        RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey))));
    }
}
