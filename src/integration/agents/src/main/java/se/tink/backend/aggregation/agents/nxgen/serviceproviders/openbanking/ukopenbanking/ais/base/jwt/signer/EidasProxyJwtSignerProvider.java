package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import java.security.cert.X509Certificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.AgentConfigurationControllerContext;
import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.JwksClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.JwksKeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@Slf4j
public final class EidasProxyJwtSignerProvider {
    private static final String CERT_ID = "UKOB";
    private final AgentComponentProvider agentComponentProvider;
    private final UkOpenBankingClientConfigurationAdapter configuration;
    private final AgentConfigurationControllerContext agentConfigurationControllerContext;
    private final InternalEidasProxyConfiguration internalEidasProxyConfiguration;
    private final EidasContext eidasContext;
    private final Class<? extends UkOpenBankingClientConfigurationAdapter> configurationFormatClass;

    public EidasProxyJwtSignerProvider(
            UkOpenBankingClientConfigurationAdapter configuration,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            Class<? extends UkOpenBankingClientConfigurationAdapter> configurationFormatClass) {
        this.configuration = configuration;
        this.agentComponentProvider = agentComponentProvider;
        this.agentConfigurationControllerContext = agentComponentProvider.getContext();
        this.internalEidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy().toInternalConfig();
        this.eidasContext = agentComponentProvider.getContext();
        this.configurationFormatClass = configurationFormatClass;
    }

    @SneakyThrows
    public JwtSigner get() {
        String appId = agentComponentProvider.getContext().getAppId();
        return CertificateUtils.getRootX509CertificateFromBase64EncodedString(
                        agentConfigurationControllerContext
                                .getAgentConfigurationController()
                                .getAgentConfiguration(configurationFormatClass)
                                .getQsealc())
                .map(x509Certificate -> constructEidasProxyJwtSigner(appId, x509Certificate))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "Could not find the OBSEAL for application with appId `%s`",
                                                appId)));
    }

    private JwtSigner constructEidasProxyJwtSigner(String appId, X509Certificate certificate) {
        KeyIdProvider keyIdProvider =
                new JwksKeyIdProvider(
                        new JwksClient(agentComponentProvider.getTinkHttpClient()),
                        configuration.getSoftwareStatementAssertions().getJwksEndpoint(),
                        certificate);
        log.info(
                "Initializing eIDAS JWT signer for appId '{}' with '{}' certificate",
                appId,
                certificate.getSubjectDN().getName());
        return new EidasProxyJwtSigner(keyIdProvider, createEidasJwsSigner());
    }

    private EidasJwsSigner createEidasJwsSigner() {
        EidasIdentity identity =
                new EidasIdentity(
                        eidasContext.getClusterId(), eidasContext.getAppId(), CERT_ID, "");
        log.info("Eidas Identity setting: `{}`", identity);
        return new EidasJwsSigner(internalEidasProxyConfiguration, identity);
    }
}
