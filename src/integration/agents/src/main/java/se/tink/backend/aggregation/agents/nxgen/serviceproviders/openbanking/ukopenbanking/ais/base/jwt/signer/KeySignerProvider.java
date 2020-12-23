package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.AgentConfigurationControllerContext;
import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.JwksClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.JwksKidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@Slf4j
public final class KeySignerProvider implements Provider<JwtSigner> {
    private static final int ALLOWED_PERCENT_OF_TRAFFIC = 5;
    private static final Predicate<RandomValueGenerator> SHOULD_USE_EIDAS_PROXY =
            generator -> ALLOWED_PERCENT_OF_TRAFFIC > generator.generateRandomDoubleInRange(0, 100);
    private final AgentComponentProvider agentComponentProvider;
    private final UkOpenBankingConfiguration configuration;
    private final AgentConfigurationControllerContext agentConfigurationControllerContext;
    private final InternalEidasProxyConfiguration internalEidasProxyConfiguration;
    private final EidasContext eidasContext;
    // TODO this is temporary solution until we will able to fetch CERT_ID from TPA on the Agent
    // setup
    private final String eidasCertId;

    @Inject
    KeySignerProvider(
            UkOpenBankingConfiguration configuration,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            @Named("eidasCertId") String eidasCertId) {
        this.configuration = configuration;
        this.agentComponentProvider = agentComponentProvider;
        this.agentConfigurationControllerContext = agentComponentProvider.getContext();
        this.internalEidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy().toInternalConfig();
        this.eidasContext = agentComponentProvider.getContext();
        this.eidasCertId = eidasCertId;
    }

    @Override
    @SneakyThrows
    public JwtSigner get() {
        String appId = agentComponentProvider.getContext().getAppId();
        SecretServiceJwtSigner secretServiceJwtSigner = new SecretServiceJwtSigner(configuration);

        List<X509Certificate> certChain = extractQsealCertChain();
        if (certChain.isEmpty()) {
            log.error(
                    "Could not extract cert chain from provided qsealc string: '{}'. Unable to construct Eidas JWT signer - falling back to secrets JWT Signer",
                    agentConfigurationControllerContext
                            .getAgentConfigurationController()
                            .getAgentConfiguration(UkOpenBankingConfiguration.class)
                            .getQsealc());

            return secretServiceJwtSigner;
        } else if (SHOULD_USE_EIDAS_PROXY.test(agentComponentProvider.getRandomValueGenerator())) {
            return constructEidasProxyJwtSigner(appId, secretServiceJwtSigner, certChain);
        } else {
            log.info("Initializing legacy JWT signer for appId '{}'", appId);
            return secretServiceJwtSigner;
        }
    }

    private JwtSigner constructEidasProxyJwtSigner(
            String appId,
            SecretServiceJwtSigner secretServiceJwtSigner,
            List<X509Certificate> certChain) {
        X509Certificate certificate = certChain.get(0);

        KidProvider kidProvider =
                new JwksKidProvider(
                        new JwksClient(agentComponentProvider.getTinkHttpClient()),
                        configuration.getSoftwareStatementAssertions().getJwksEndpoint(),
                        certificate);
        EidasProxyJwtSigner eidasProxyJwtSigner =
                new EidasProxyJwtSigner(kidProvider, createEidasJwsSigner());
        log.info(
                "Initializing eIDAS JWT signer for appId '{}' with '{}' certificate",
                appId,
                certificate.getSubjectDN().getName());
        return new EidasProxyWithFallbackJwtSigner(eidasProxyJwtSigner, secretServiceJwtSigner);
    }

    private List<X509Certificate> extractQsealCertChain() throws CertificateException {
        return CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                agentConfigurationControllerContext
                        .getAgentConfigurationController()
                        .getAgentConfiguration(UkOpenBankingConfiguration.class)
                        .getQsealc());
    }

    private EidasJwsSigner createEidasJwsSigner() {
        return new EidasJwsSigner(
                internalEidasProxyConfiguration,
                new EidasIdentity(
                        eidasContext.getClusterId(), eidasContext.getAppId(), eidasCertId, ""));
    }
}
