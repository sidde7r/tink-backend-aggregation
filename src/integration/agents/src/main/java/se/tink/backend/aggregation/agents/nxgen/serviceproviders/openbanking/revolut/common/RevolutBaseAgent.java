package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidatorDisabled;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

@Slf4j
public abstract class RevolutBaseAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final UnleashClient unleashClient;
    private final String currentAppId;
    private final String currentProviderName;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipTypes(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(RevolutConstants.ORGANISATION_ID)
                        .withApiBaseURL(RevolutConstants.AIS_API_URL)
                        .withWellKnownURL(RevolutConstants.WELL_KNOWN_URL)
                        .build();
    }

    public RevolutBaseAgent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(
                componentProvider,
                flowFacade,
                aisConfig,
                new UkOpenBankingPisConfiguration(
                        RevolutConstants.PIS_API_URL, RevolutConstants.WELL_KNOWN_URL),
                createPisRequestFilterUsingPs256MinimalSignature(
                        flowFacade.getJwtSinger(), componentProvider.getRandomValueGenerator()));

        this.unleashClient = componentProvider.getUnleashClient();
        this.currentProviderName = request.getCredentials().getProviderName();
        this.currentAppId = componentProvider.getContext().getAppId();
    }

    @Override
    protected TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new UkOpenBankingTransferDestinationFetcher(
                        new RevolutTransferDestinationAccountsProvider(apiClient),
                        AccountIdentifierType.IBAN,
                        IbanIdentifier.class));
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        DefaultIdentifierMapper identifierMapper = new DefaultIdentifierMapper(valueExtractor);

        return new UkOpenBankingV31Ais(
                aisConfig,
                persistentStorage,
                localDateTimeSource,
                new CreditCardAccountMapper(
                        new DefaultCreditCardBalanceMapper(valueExtractor), identifierMapper),
                new RevolutTransactionalAccountMapperDecorator(
                        new RevolutTransactionalAccountMapper(
                                new TransactionalAccountBalanceMapper(valueExtractor),
                                identifierMapper)));
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
                new UkOpenBankingAisAuthenticator(this.apiClient, aisConfig.getPermissions()),
                this.credentials,
                this.strongAuthenticationState,
                this.request.getCallbackUri(),
                this.randomValueGenerator,
                new OpenIdAuthenticationValidator(this.apiClient),
                getConsentStatusValidation(unleashClient, currentAppId, currentProviderName));
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

    private ConsentStatusValidator getConsentStatusValidation(
            UnleashClient unleashClient, String currentAppId, String currentProviderName) {

        Toggle toggle =
                Toggle.of("revolut-consent-status-validation")
                        .context(
                                UnleashContext.builder()
                                        .addProperty(
                                                Constants.Context.PROVIDER_NAME.getValue(),
                                                currentProviderName)
                                        .addProperty(
                                                Constants.Context.APP_ID.getValue(), currentAppId)
                                        .build())
                        .build();

        if (unleashClient.isToggleEnable(toggle)) {
            return new ConsentStatusValidator(apiClient, persistentStorage);
        } else {
            return new ConsentStatusValidatorDisabled(apiClient, persistentStorage);
        }
    }
}
