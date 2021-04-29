package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

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
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA})
public class DanskeBankV31BusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(DanskeBankConstants.ORGANISATION_ID)
                        .withApiBaseURL(DanskeBankConstants.AIS_API_URL)
                        .withWellKnownURL(DanskeBankConstants.BUSINESS_WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.PARTY)
                        .build();
    }

    @Inject
    public DanskeBankV31BusinessAgent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(componentProvider, flowFacade, aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        CreditCardAccountMapper creditCardAccountMapper =
                new CreditCardAccountMapper(
                        new DefaultCreditCardBalanceMapper(valueExtractor),
                        new DanskeBankIdentifierMapper(valueExtractor));
        return new UkOpenBankingV31Ais(
                aisConfig,
                persistentStorage,
                localDateTimeSource,
                creditCardAccountMapper,
                UkOpenBankingV31Ais.defaultTransactionalAccountMapper());
    }

    @Override
    public Authenticator constructAuthenticator() {
        UkOpenBankingAisAuthenticationController authController = createUkObAuthController();

        return createAutoAuthController(authController);
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
