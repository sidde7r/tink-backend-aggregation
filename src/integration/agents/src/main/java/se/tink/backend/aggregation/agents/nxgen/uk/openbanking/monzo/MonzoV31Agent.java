package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.FASTER_PAYMENTS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingFlowModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.RequiredReferenceRemittanceInfoDomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.RequiredReferenceRemittanceInfoDomesticSchedulerPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.PaymentRequestWithRequiredReferenceRemittanceInfoValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.consent.MonzoConsentExpirationFilter;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

@Slf4j
@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
@AgentPisCapability(capabilities = FASTER_PAYMENTS, markets = "GB")
public final class MonzoV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipTypes(
                                AccountOwnershipType.PERSONAL, AccountOwnershipType.BUSINESS)
                        .withOrganisationId(MonzoConstants.ORGANISATION_ID)
                        .withApiBaseURL(MonzoConstants.AIS_API_URL)
                        .withWellKnownURL(MonzoConstants.WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.PARTY)
                        .build();
    }

    private final AgentComponentProvider componentProvider;

    @Inject
    public MonzoV31Agent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(
                componentProvider,
                flowFacade,
                aisConfig,
                new UkOpenBankingPisConfiguration(
                        MonzoConstants.PIS_API_URL, MonzoConstants.WELL_KNOWN_URL),
                createPisRequestFilterUsingPs256WithoutBase64Signature(
                        flowFacade.getJwtSinger(), componentProvider.getRandomValueGenerator()));
        this.componentProvider = componentProvider;
        client.addFilter(new MonzoConsentExpirationFilter(persistentStorage));
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        Toggle toggle =
                Toggle.of("uk-monzo-trx-fix")
                        .context(
                                UnleashContext.builder()
                                        .addProperty(
                                                Constants.Context.PROVIDER_NAME.getValue(),
                                                "uk-monzo-oauth2")
                                        .addProperty(
                                                Constants.Context.APP_ID.getValue(),
                                                componentProvider.getContext().getAppId())
                                        .build())
                        .build();

        if (componentProvider.getUnleashClient().isToggleEnable(toggle)) {
            log.info("[NEW TRANSACTION FETCHING]");
            return new MonzoV31Ais(
                    aisConfig, persistentStorage, localDateTimeSource, apiClient, request);
        } else {
            log.info("[BUGGY TRANSACTION FETCHING]");
            return new MonzoV31AisWithBuggyTransactionFetching(
                    aisConfig, persistentStorage, localDateTimeSource, apiClient);
        }
    }

    @Override
    protected UkOpenBankingPaymentRequestValidator getPaymentRequestValidator() {
        return new PaymentRequestWithRequiredReferenceRemittanceInfoValidator();
    }

    @Override
    protected DomesticPaymentConverter getDomesticPaymentConverter() {
        return new RequiredReferenceRemittanceInfoDomesticPaymentConverter();
    }

    @Override
    protected DomesticScheduledPaymentConverter getDomesticScheduledPaymentConverter() {
        return new RequiredReferenceRemittanceInfoDomesticSchedulerPaymentConverter();
    }

    @Override
    public Authenticator constructAuthenticator() {
        UkOpenBankingAisAuthenticationController authController = createUkObAuthController();

        return createAutoAuthController(authController);
    }

    private UkOpenBankingAisAuthenticationController createUkObAuthController() {
        return new UkOpenBankingAisAuthenticationController(
                this.persistentStorage,
                this.supplementalInformationHelper,
                this.apiClient,
                new UkOpenBankingAisAuthenticator(this.apiClient),
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
