package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.firstdirect;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.PIS_UK_FASTER_PAYMENT;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.PartyMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.RequiredReferenceRemittanceInfoDomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.RequiredReferenceRemittanceInfoDomesticSchedulerPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.PaymentRequestWithRequiredReferenceRemittanceInfoValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.HsbcGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.signature.HsbcSignatureCreator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.identitydata.IdentityData;

@Slf4j
@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
@AgentPisCapability(capabilities = PIS_UK_FASTER_PAYMENT, markets = "GB")
public final class FirstDirectV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private static final UkOpenBankingPisConfiguration pisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(FirstDirectConstants.ORGANISATION_ID)
                        .withApiBaseURL(FirstDirectConstants.AIS_API_URL)
                        .withWellKnownURL(FirstDirectConstants.WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.ACCOUNT_ID_PARTY)
                        .build();
        pisConfig =
                new UkOpenBankingPisConfiguration(
                        FirstDirectConstants.PIS_API_URL, FirstDirectConstants.WELL_KNOWN_URL);
    }

    @Inject
    public FirstDirectV31Agent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(
                componentProvider,
                flowFacade,
                aisConfig,
                pisConfig,
                createPisRequestFilter(
                        new HsbcSignatureCreator(flowFacade.getJwtSinger()),
                        flowFacade.getJwtSinger(),
                        componentProvider.getRandomValueGenerator()));
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {

        // FD is hsbc subsidiary so need to have the same overridden client
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

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.CHECKING_ACCOUNTS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.CHECKING_ACCOUNTS);
            return new FetchAccountsResponse(Collections.emptyList());
        }

        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.CHECKING_TRANSACTIONS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.CHECKING_TRANSACTIONS);
            return new FetchTransactionsResponse(Collections.emptyMap());
        }

        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.SAVING_ACCOUNTS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.SAVING_ACCOUNTS);
            return new FetchAccountsResponse(Collections.emptyList());
        }

        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.SAVING_TRANSACTIONS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.SAVING_TRANSACTIONS);
            return new FetchTransactionsResponse(Collections.emptyMap());
        }

        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.CREDITCARD_ACCOUNTS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.CREDITCARD_ACCOUNTS);
            return new FetchAccountsResponse(Collections.emptyList());
        }

        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.CREDITCARD_ACCOUNTS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.CREDITCARD_ACCOUNTS);
            return new FetchTransactionsResponse(Collections.emptyMap());
        }

        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.TRANSFER_DESTINATIONS)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.TRANSFER_DESTINATIONS);
            return new FetchTransferDestinationsResponse(Collections.emptyMap());
        }

        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.LIST_BENEFICIARIES)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.LIST_BENEFICIARIES);
            return new FetchTransferDestinationsResponse(Collections.emptyMap());
        }

        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        FetchIdentityDataResponse responseWithEmptyIdentityData =
                new FetchIdentityDataResponse(
                        IdentityData.builder().setFullName(null).setDateOfBirth(null).build());

        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.LIST_BENEFICIARIES)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.LIST_BENEFICIARIES);
            return responseWithEmptyIdentityData;
        }

        return getAisSupport()
                .makePartyFetcher(apiClient)
                .fetchParty()
                .map(PartyMapper::toIdentityData)
                .map(FetchIdentityDataResponse::new)
                .orElse(responseWithEmptyIdentityData);
    }
}
