package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.agents.rpc.Provider.AuthenticationFlow;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterIds;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterSpecificCallbacks;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankAppToAppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankDecoupledAppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockDkNemIdReAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockNoBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockSeBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMultiRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAnd2FAWithTemplatesAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAndOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transferdestinations.DemobankTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.filters.AuthenticationErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankDtoMappers;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.DemobankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentRequestFilter;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankRecurringPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankSinglePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error.DemobankErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer.DemobankPaymentEmbeddedSigner;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer.DemobankPaymentMockBankIdSigner;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer.DemobankPaymentRedirectSigner;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer.DemobankPaymentSigner;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.agents.payments.TypedPaymentControllerable;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, IDENTITY_DATA, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS,
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        },
        markets = {"IT"})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        },
        markets = {"DE", "ES", "FR", "FI"})
@AgentPisCapability(
        capabilities = {
            PisCapability.FASTER_PAYMENTS,
        },
        markets = {"GB"})
@AgentPisCapability(
        capabilities = {
            PisCapability.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER,
            PisCapability.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.SEPA_CREDIT_TRANSFER,
        },
        markets = {"NO"})
public final class DemobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshIdentityDataExecutor,
                TypedPaymentControllerable,
                RefreshTransferDestinationExecutor {
    protected final DemobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final String callbackUri;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public DemobankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.callbackUri = getCallbackUri();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        apiClient = new DemobankApiClient(client, persistentStorage, callbackUri);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        transferDestinationRefreshController = constructTransferDestinationController();
        creditCardRefreshController = constructCreditCardRefreshController();
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new AuthenticationErrorFilter());
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new DemobankTransferDestinationFetcher());
    }

    private String getCallbackUri() {
        String callbackUri = request.getCallbackUri();
        if (callbackUri == null || callbackUri.trim().length() == 0) {
            switch (context.getClusterId()) {
                case ClusterIds.OXFORD_STAGING:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_STAGING_CALLBACK;
                    break;
                case ClusterIds.OXFORD_PREPROD:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PREPROD_CALLBACK;
                    break;
                default:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PROD_CALLBACK;
            }
        }
        return callbackUri;
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final DemobankTransactionalAccountFetcher demobankTransactionalAccountFetcher =
                new DemobankTransactionalAccountFetcher(apiClient, provider);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                demobankTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                demobankTransactionalAccountFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final DemobankCreditCardFetcher demobankCreditCardFetcher =
                new DemobankCreditCardFetcher(apiClient, provider);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                demobankCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController(demobankCreditCardFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new DemobankSessionHandler(persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (CredentialsTypes.MOBILE_BANKID.equals(provider.getCredentialsType())) {
            if (MarketCode.NO.toString().equals(provider.getMarket())) {
                return new BankIdAuthenticationControllerNO(
                        supplementalInformationController,
                        new DemobankMockNoBankIdAuthenticator(apiClient),
                        catalog);
            } else if (MarketCode.SE.toString().equals(provider.getMarket())) {
                return new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new DemobankMockSeBankIdAuthenticator(apiClient),
                        persistentStorage,
                        request);
            }
        } else if (CredentialsTypes.THIRD_PARTY_APP.equals(provider.getCredentialsType())
                && AccessType.OTHER.equals(provider.getAccessType())
                && MarketCode.DK.toString().equals(provider.getMarket())) {
            return constructMockNemIdAuthenticator();
        } else if (AccessType.OPEN_BANKING.equals(provider.getAccessType())) {
            if (AuthenticationFlow.DECOUPLED.equals(provider.getAuthenticationFlow())) {
                return constructDecoupledAppAuthenticator();
            } else if (AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())) {
                if (hasTemplateAuthentication()) {
                    return constructPasswordAndOtpWithTemplatesAuthenticator();
                }
                return constructPasswordAndOtpAuthenticator();
            } else if (hasAppToAppAuthentication()) {
                return constructApptToAppAuthenticator();
            }
            return constructRedirectAuthenticator();
        } else if (CredentialsTypes.PASSWORD.equals(provider.getCredentialsType())) {
            return new PasswordAuthenticationController(
                    new DemobankPasswordAuthenticator(apiClient));
        }
        throw new IllegalStateException("Invalid provider configuration");
    }

    private Authenticator constructPasswordAndOtpAuthenticator() {
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(persistentStorage, apiClient);
        DemobankPasswordAndOtpAuthenticator authenticator =
                new DemobankPasswordAndOtpAuthenticator(
                        apiClient, supplementalInformationController);
        return new AutoAuthenticationController(request, context, authenticator, autoAuthenticator);
    }

    private boolean hasTemplateAuthentication() {
        return provider.getName().endsWith("-templates");
    }

    private Authenticator constructPasswordAndOtpWithTemplatesAuthenticator() {
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(persistentStorage, apiClient);
        DemobankPasswordAnd2FAWithTemplatesAuthenticator authenticator =
                new DemobankPasswordAnd2FAWithTemplatesAuthenticator(
                        apiClient, supplementalInformationController);
        return new AutoAuthenticationController(request, context, authenticator, autoAuthenticator);
    }

    private Authenticator constructMockNemIdAuthenticator() {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        DemobankMockDkNemIdReAuthenticator demobankNemIdAuthenticator =
                new DemobankMockDkNemIdReAuthenticator(
                        apiClient,
                        client,
                        persistentStorage,
                        username,
                        password,
                        this.randomValueGenerator);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new NemIdCodeAppAuthenticationController(
                        demobankNemIdAuthenticator, supplementalInformationController, catalog),
                demobankNemIdAuthenticator);
    }

    private Authenticator constructDecoupledAppAuthenticator() {
        DemobankDecoupledAppAuthenticator demobankDecoupledAppAuthenticator =
                new DemobankDecoupledAppAuthenticator(apiClient, supplementalInformationController);
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(persistentStorage, apiClient);
        return new AutoAuthenticationController(
                request, systemUpdater, demobankDecoupledAppAuthenticator, autoAuthenticator);
    }

    private boolean hasAppToAppAuthentication() {
        return provider.getName().contains("-app-to-app");
    }

    private Authenticator constructApptToAppAuthenticator() {
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(persistentStorage, apiClient);
        DemobankAppToAppAuthenticator authenticator =
                new DemobankAppToAppAuthenticator(
                        apiClient,
                        credentials,
                        getCallbackUri(),
                        strongAuthenticationState.getState());
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        authenticator, supplementalInformationHelper),
                autoAuthenticator);
    }

    private Authenticator constructRedirectAuthenticator() {
        DemobankMultiRedirectAuthenticator multiRedirectAuthenticator =
                new DemobankMultiRedirectAuthenticator(
                        apiClient,
                        persistentStorage,
                        callbackUri,
                        supplementalInformationHelper,
                        request,
                        strongAuthenticationState);

        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(persistentStorage, apiClient);

        return new AutoAuthenticationController(
                request, context, multiRedirectAuthenticator, autoAuthenticator);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new DemobankIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public Optional<PaymentController> getPaymentController(Payment payment) {
        final DemobankStorage storage = new DemobankStorage();
        final DemobankPaymentApiClient paymentApiClient =
                constructPaymentApiClient(storage, payment);
        final DemobankPaymentSigner signer = constructPaymentSigner(paymentApiClient, storage);

        final DemobankPaymentExecutor paymentExecutor =
                new DemobankPaymentExecutor(paymentApiClient, signer, storage);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    private DemobankPaymentApiClient constructPaymentApiClient(
            DemobankStorage storage, Payment payment) {
        final DemobankDtoMappers mappers = new DemobankDtoMappers();
        final DemobankPaymentRequestFilter requestFilter =
                new DemobankPaymentRequestFilter(storage);
        final DemobankErrorHandler errorHandler = new DemobankErrorHandler();

        return PaymentServiceType.PERIODIC.equals(payment.getPaymentServiceType())
                ? new DemobankRecurringPaymentApiClient(
                        mappers, errorHandler, requestFilter, storage, client, callbackUri)
                : new DemobankSinglePaymentApiClient(
                        mappers, errorHandler, requestFilter, storage, client, callbackUri);
    }

    private DemobankPaymentSigner constructPaymentSigner(
            DemobankPaymentApiClient apiClient, DemobankStorage storage) {

        if (AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())) {
            return new DemobankPaymentEmbeddedSigner(
                    apiClient, storage, supplementalInformationController, credentials);

        } else if (CredentialsTypes.MOBILE_BANKID.equals(provider.getCredentialsType())) {

            if (MarketCode.SE.toString().equals(provider.getMarket())) {
                BankIdAuthenticationController<String> bankIdAuthenticationController =
                        new BankIdAuthenticationController<>(
                                supplementalInformationController,
                                new DemobankMockSeBankIdAuthenticator(this.apiClient),
                                persistentStorage,
                                request);
                return new DemobankPaymentMockBankIdSigner(
                        apiClient,
                        storage,
                        bankIdAuthenticationController,
                        credentials,
                        persistentStorage);
            }

        } else {
            return new DemobankPaymentRedirectSigner(
                    apiClient,
                    storage,
                    supplementalInformationHelper,
                    strongAuthenticationState,
                    callbackUri);
        }
        throw new IllegalStateException("Invalid provider configuration");
    }
}
