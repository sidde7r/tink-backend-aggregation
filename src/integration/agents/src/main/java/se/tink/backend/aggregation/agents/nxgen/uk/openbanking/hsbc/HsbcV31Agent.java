package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.JwtSignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.validator.HsbcPaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentDependencyModulesForProductionMode(
        modules = {UkOpenBankingModule.class, JwtSignerModule.class})
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
public final class HsbcV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private static final UkOpenBankingPisConfiguration pisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                UKOpenBankingAis.builder()
                        .withOrganisationId(HsbcConstants.ORGANISATION_ID)
                        .withApiBaseURL(HsbcConstants.PERSONAL_AIS_API_URL)
                        .withWellKnownURL(HsbcConstants.PERSONAL_WELL_KNOWN_URL)
                        .withAppToAppURL(HsbcConstants.PERSONAL_APP_TO_APP_AUTH_URL)
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .build();

        pisConfig =
                new UkOpenBankingPisConfiguration(
                        HsbcConstants.ORGANISATION_ID,
                        HsbcConstants.PERSONAL_PIS_API_URL,
                        HsbcConstants.PERSONAL_WELL_KNOWN_URL);
    }

    @Inject
    public HsbcV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                pisConfig,
                new HsbcPaymentRequestValidator());
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {

        return new HsbcGroupApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig);
    }
}
