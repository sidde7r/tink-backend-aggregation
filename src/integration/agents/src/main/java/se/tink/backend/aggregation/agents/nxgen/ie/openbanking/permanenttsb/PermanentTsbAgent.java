package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb.PermanentTsbConstants.HeaderFormats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingEUFlowModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingEUFlowModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public class PermanentTsbAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfiguration aisConfig;
    private final AgentComponentProvider componentProvider;
    private final String qsealPem;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(PermanentTsbConstants.ORGANISATION_ID)
                        .withWellKnownURL(PermanentTsbConstants.WELL_KNOWN_URL)
                        .withApiBaseURL(PermanentTsbConstants.AIS_API_URL)
                        .build();
    }

    @Inject
    public PermanentTsbAgent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(componentProvider, flowFacade, aisConfig);
        this.componentProvider = componentProvider;

        final AgentConfiguration<UkOpenBankingConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(UkOpenBankingConfiguration.class);
        qsealPem = getQsealCertificateWithBeginAndEndAnchorLines(agentConfiguration);
    }

    @Override
    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {

        return new PermanentTsbApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig,
                componentProvider,
                qsealPem);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new PermanentTsbAis(aisConfig, persistentStorage, localDateTimeSource);
    }

    private static String getQsealCertificateWithBeginAndEndAnchorLines(
            AgentConfiguration<UkOpenBankingConfiguration> agentConfiguration) {
        try {
            String qsealCertificate =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());

            return String.format(HeaderFormats.CERTIFICATE_FORMAT, qsealCertificate);
        } catch (CertificateException e) {
            throw new IllegalStateException("Failed to extract Qsealc from agent configuration", e);
        }
    }
}
