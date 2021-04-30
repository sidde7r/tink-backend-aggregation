package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingFlowModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.PartyMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.identitydata.IdentityData;

@Slf4j
@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class BankOfScotlandBusinessV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL(BankOfScotlandConstants.AIS_API_URL)
                        .withWellKnownURL(BankOfScotlandConstants.WELL_KNOWN_URL_BUSINESS)
                        .withOrganisationId(BankOfScotlandConstants.ORGANIZATION_ID)
                        .withAllowedAccountOwnershipType(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public BankOfScotlandBusinessV31Agent(
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

        if (allowedItemsValidator.isForbiddenToBeRefreshed(RefreshableItem.IDENTITY_DATA)) {
            log.info(FETCHING_FORBIDDEN_FOR_ITEM_MSG, RefreshableItem.IDENTITY_DATA);
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
