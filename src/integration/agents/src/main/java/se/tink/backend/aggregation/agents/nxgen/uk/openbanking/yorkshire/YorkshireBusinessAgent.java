package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.yorkshire;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingFlowModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public class YorkshireBusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(YorkshireConstants.ORGANISATION_ID)
                        .withWellKnownURL(YorkshireConstants.WELL_KNOWN_URL)
                        .withApiBaseURL(YorkshireConstants.AIS_API_URL)
                        .withPartyEndpoints(PartyEndpoint.ACCOUNT_ID_PARTY, PartyEndpoint.PARTY)
                        .withAllowedAccountOwnershipTypes(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public YorkshireBusinessAgent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(componentProvider, flowFacade, aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    public Authenticator constructAuthenticator() {
        UkOpenBankingAisAuthenticationController authController = createUkObAuthController();

        return createAutoAuthController(authController);
    }

    @Override
    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {

        return new YorkshireApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig);
    }

    private UkOpenBankingAisAuthenticationController createUkObAuthController() {
        return new UkOpenBankingAisAuthenticationController(
                this.persistentStorage,
                this.supplementalInformationHelper,
                this.apiClient,
                new UkOpenBankingAisAuthenticator(this.apiClient, aisConfig.getPermissions()),
                this.credentials,
                this.strongAuthenticationState,
                this.request.getCallbackUri(),
                this.randomValueGenerator,
                new OpenIdAuthenticationValidator(this.apiClient),
                new ConsentStatusValidator(this.apiClient, this.persistentStorage));
    }

    private AutoAuthenticationController createAutoAuthController(
            UkOpenBankingAisAuthenticationController authController) {
        return new AutoAuthenticationController(
                this.request,
                this.systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        authController, this.supplementalInformationHelper),
                authController);
    }
}
