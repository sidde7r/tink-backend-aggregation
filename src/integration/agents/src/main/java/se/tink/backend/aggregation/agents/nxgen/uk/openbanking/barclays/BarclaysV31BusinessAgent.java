package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.Urls.V31;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingLocalKeySignerModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class BarclaysV31BusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    static {
        aisConfig =
                UKOpenBankingAis.builder()
                        .withOrganisationId(BarclaysConstants.ORGANISATION_ID)
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.BUSINESS_WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES)
                        .withAllowedAccountOwnershipType(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public BarclaysV31BusinessAgent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(componentProvider, jwtSigner, aisConfig, true);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        UkOpenBankingV31Ais ukOpenBankingV31Ais =
                new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
        return new BarclaysV31Ais(ukOpenBankingV31Ais, aisConfig);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        UKOpenbankingV31Executor ukOpenbankingV31Executor =
                new UKOpenbankingV31Executor(
                        pisConfig,
                        softwareStatement,
                        providerConfiguration,
                        apiClient,
                        supplementalInformationHelper,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator);
        return Optional.of(
                new PaymentController(ukOpenbankingV31Executor, ukOpenbankingV31Executor));
    }
}
