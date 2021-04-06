package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.AgentConfigurationControllerContext;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.JwksClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.JwksKeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasJwsSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyWithFallbackJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.EidasJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@Slf4j
class DanskeJwtSignerProvider implements Provider<JwtSigner> {

    private final AgentConfigurationControllerContext agentConfigurationControllerContext;
    private final CompositeAgentContext agentContext;
    private final AgentComponentProvider agentComponentProvider;
    private final InternalEidasProxyConfiguration internalEidasProxyConfiguration;
    private final DanskebankEUConfiguration configuration;
    private final String appId;

    @Inject
    DanskeJwtSignerProvider(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            DanskebankEUConfiguration configuration) {
        this.agentConfigurationControllerContext = agentComponentProvider.getContext();
        this.agentContext = agentComponentProvider.getContext();
        this.agentComponentProvider = agentComponentProvider;
        this.internalEidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy().toInternalConfig();
        this.configuration = configuration;
        this.appId = agentComponentProvider.getContext().getAppId();
    }

    @Override
    public JwtSigner get() {
        EidasIdentity eidasIdentity =
                new EidasIdentity(
                        agentContext.getClusterId(),
                        agentContext.getAppId(),
                        DanskeJwtSignerProvider.class);
        EidasJwtSigner eidasFallbackJwtSigner =
                new EidasJwtSigner(internalEidasProxyConfiguration, eidasIdentity);

        Optional<X509Certificate> maybeCertificate = extractQSealCertificate();
        if (maybeCertificate.isPresent()) {
            X509Certificate certificate = maybeCertificate.get();
            return getJwtSignerWithFallback(eidasIdentity, eidasFallbackJwtSigner, certificate);
        } else {
            log.info("[Danske JWT Signer] Initializing legacy JWT signer for appId '{}'", appId);
            return eidasFallbackJwtSigner;
        }
    }

    private Optional<X509Certificate> extractQSealCertificate() {
        try {
            // There is a chain with 3 certificates.
            // We can expect the order is leaf ---> root.
            // So it is fine to pick the first one.
            return CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                            agentConfigurationControllerContext
                                    .getAgentConfigurationController()
                                    .getAgentConfiguration(DanskebankEUConfiguration.class)
                                    .getQsealc())
                    .stream()
                    .findFirst();
        } catch (CertificateException | RuntimeException e) {
            log.error(
                    "[Danske JWT Signer] There were issues while getting certificate - return empty to use fallback");
            return Optional.empty();
        }
    }

    private EidasProxyJwtSigner getEidasProxyJwtSigner(
            InternalEidasProxyConfiguration internalEidasProxyConfiguration,
            EidasIdentity identity,
            KeyIdProvider keyIdProvider) {
        EidasJwsSigner eidasJwsSigner =
                new EidasJwsSigner(internalEidasProxyConfiguration, identity);
        return new EidasProxyJwtSigner(keyIdProvider, eidasJwsSigner);
    }

    private JwtSigner getJwtSignerWithFallback(
            EidasIdentity eidasIdentity,
            EidasJwtSigner eidasFallbackJwtSigner,
            X509Certificate certificate) {
        log.info(
                "[Danske JWT Signer] Initializing eIDAS JWT signer for appId '{}' with '{}' certificate",
                appId,
                certificate.getSubjectDN().getName());
        KeyIdProvider keyIdProvider =
                new JwksKeyIdProvider(
                        new JwksClient(agentComponentProvider.getTinkHttpClient()),
                        configuration.getSoftwareStatementAssertions().getJwksEndpoint(),
                        certificate);
        EidasProxyJwtSigner eidasProxyJwtSigner =
                getEidasProxyJwtSigner(internalEidasProxyConfiguration, eidasIdentity,
                    keyIdProvider);
        return new EidasProxyWithFallbackJwtSigner(eidasProxyJwtSigner, eidasFallbackJwtSigner);
    }
}
