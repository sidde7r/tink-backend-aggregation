package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.EidasJwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.EidasJwtSigner.EidasSigningKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.EidasTlsConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationAdapter;

public class DanskebankConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty @Secret private String organizationId;
    @JsonProperty @SensitiveSecret private String clientId;
    @JsonProperty @Secret private String softwareStatementAssertion;
    @JsonProperty @Secret private String redirectUrl;
    @JsonProperty @Secret private String softwareId;
    @JsonProperty @SensitiveSecret private String signingKeyId;

    @Override
    public byte[] getRootCAData() {
        return new byte[0];
    }

    @Override
    public String getRootCAPassword() {
        return null;
    }

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        return new ProviderConfiguration(organizationId, new ClientInfo(clientId, ""));
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertion() {
        return new SoftwareStatementAssertion(softwareStatementAssertion, softwareId, redirectUrl);
    }

    @Override
    public TlsConfigurationAdapter getTlsConfigurationAdapter() {
        return new EidasTlsConfiguration();
    }

    @Override
    public JwtSigner getSigner() {
        return new EidasJwtSigner(
                ImmutableMap.<Algorithm, EidasSigningKey>builder()
                        .put(
                                Algorithm.PS256,
                                EidasSigningKey.of("PSDSE-FINA-44059", QsealcAlg.EIDAS_PSS_SHA256))
                        .put(
                                Algorithm.RS256,
                                EidasSigningKey.of(
                                        "PSDSE-FINA-44059-RSA", QsealcAlg.EIDAS_RSA_SHA256))
                        .build());
    }
}
