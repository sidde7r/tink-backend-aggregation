package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.security.cert.X509Certificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.AgentConfigurationControllerContext;
import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
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

@Slf4j
public final class KeySignerProvider implements Provider<JwtSigner> {
    private static final ImmutableSet<String> EIDAS_ENABLED_APPS =
            ImmutableSet.of(
                    "5f98e87106384b2981c0354a33b51590", // oxford-staging
                    "e643eb7981d24acfb47834ef338a4e2a", // oxford-prod
                    "c859501868b742b6bebd7a3f7911cd85", // oxford-preprod
                    "bb9275c6890a413398d92116047ebfb2", // kirkby-staging,
                    "bb7defc66be94f6ca35cb069135d350a", // kirkby-production
                    "aec3753f2f7d42ffb0fd71740f029992", // one of paypals appId
                    "19808b8ac56149e5bd079a9621987ece" // dyme appId
                    );
    private final AgentComponentProvider agentComponentProvider;
    private final UkOpenBankingClientConfigurationAdapter configuration;
    private final AgentConfigurationControllerContext agentConfigurationControllerContext;
    private final InternalEidasProxyConfiguration internalEidasProxyConfiguration;
    private final EidasContext eidasContext;
    // TODO this is temporary solution until we will able to fetch CERT_ID from TPA on the Agent
    // setup
    private final String eidasCertId;

    @Inject
    private KeySignerProvider(
            UkOpenBankingClientConfigurationAdapter configuration,
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
        // this is temporary solution, it should be deleted after the Tink and all of our clients
        // migration certificates to the eIDAS proxy
        if (EIDAS_ENABLED_APPS.contains(appId)) {
            log.info("Initialize eIDAS JWT signer for '{}'", appId);
            X509Certificate certificate =
                    CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                                    agentConfigurationControllerContext
                                            .getAgentConfigurationController()
                                            .getAgentConfiguration(UkOpenBankingConfiguration.class)
                                            .getQsealc())
                            .get(0);
            KidProvider kidProvider =
                    new JwksKidProvider(
                            new JwksClient(agentComponentProvider.getTinkHttpClient()),
                            configuration.getSoftwareStatementAssertions().getJwksEndpoint(),
                            certificate);
            return new EidasJwtSigner(kidProvider, createEidasJwsSigner());
        } else {
            return new LocalJwtSigner((UkOpenBankingConfiguration) configuration);
        }
    }

    private EidasJwsSigner createEidasJwsSigner() {
        return new EidasJwsSigner(
                internalEidasProxyConfiguration,
                new EidasIdentity(
                        eidasContext.getClusterId(), eidasContext.getAppId(), eidasCertId, ""));
    }
}
