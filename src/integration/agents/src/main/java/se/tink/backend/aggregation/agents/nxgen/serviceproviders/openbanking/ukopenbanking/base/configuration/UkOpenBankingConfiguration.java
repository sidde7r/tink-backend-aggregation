package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.LocalKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.LocalCertificateTlsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationAdapter;

@JsonObject
public class UkOpenBankingConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty @Secret private String organizationId;
    @JsonProperty @SensitiveSecret private String clientId;
    @JsonProperty @Secret private String signingKey;
    @JsonProperty @Secret private String signingKeyId;
    @JsonProperty @Secret private String softwareStatementAssertion;
    @JsonProperty @Secret private String redirectUrl;
    @JsonProperty @Secret private String softwareId;
    @JsonProperty @Secret private String transportKey;
    @JsonProperty @Secret private String transportKeyId;
    @JsonProperty @Secret private String rootCAData;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @SensitiveSecret private String transportKeyPassword;
    @JsonProperty @SensitiveSecret private String signingKeyPassword;
    @JsonProperty @SensitiveSecret private String rootCAPassword;

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

    public String getRedirectUrl() {
        return redirectUrl;
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

    @Override
    public byte[] getRootCAData() {
        return EncodingUtils.decodeBase64String(rootCAData);
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
    public String getRootCAPassword() {
        return rootCAPassword;
    }

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        return new ProviderConfiguration(organizationId, new ClientInfo(clientId, clientSecret));
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertion() {
        return new SoftwareStatementAssertion(softwareStatementAssertion, softwareId, redirectUrl);
    }

    @Override
    public TlsConfigurationAdapter getTlsConfigurationAdapter() {
        return new LocalCertificateTlsConfiguration(
                transportKeyId, transportKey, transportKeyPassword);
    }

    @Override
    public JwtSigner getSigner() {
        return new LocalKeySigner(
                signingKeyId,
                RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey)));
    }
}
