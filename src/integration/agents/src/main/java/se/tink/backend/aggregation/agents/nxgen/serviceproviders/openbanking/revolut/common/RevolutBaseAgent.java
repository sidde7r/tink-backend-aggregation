package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public abstract class RevolutBaseAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final LocalDateTimeSource localDateTimeSource;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
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
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
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
