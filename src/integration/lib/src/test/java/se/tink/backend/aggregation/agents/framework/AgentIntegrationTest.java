package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentClassFactory;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.configuration.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid.ConstantUUIDSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid.RandomUUIDSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.MockSupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.NextGenTinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.WireMockTinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.framework.validation.AisValidator;
import se.tink.backend.aggregation.nxgen.framework.validation.ValidatorFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.utils.masker.CredentialsStringMaskerBuilder;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClientImpl;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class AgentIntegrationTest extends AbstractConfigurationBase {

    private static final Logger log = LoggerFactory.getLogger(AbstractAgentTest.class);

    private final Provider provider;
    private final User user;
    private final boolean loadCredentialsBefore;
    private final boolean saveCredentialsAfter;
    private final boolean requestFlagManual;
    private final boolean doLogout;
    private final boolean expectLoggedIn;
    private final Set<RefreshableItem> refreshableItems;
    private final AisValidator validator;
    private final NewAgentTestContext context;
    private final SupplementalInformationController supplementalInformationController;
    private final String redirectUrl;
    private final String clusterIdForSecretsService;
    private Credentials credential;
    // if it should override standard logic (Todo: find a better way to implement this!)
    private Boolean requestFlagCreate;
    private Boolean requestFlagUpdate;

    private String wireMockServerHost;
    private final Map<String, String> mockCallbackData;

    protected AgentIntegrationTest(Builder builder) {
        this.provider = builder.getProvider();
        this.user = builder.getUser();
        this.credential = builder.getCredential();
        this.loadCredentialsBefore = builder.isLoadCredentialsBefore();
        this.saveCredentialsAfter = builder.isSaveCredentialsAfter();
        this.requestFlagCreate = builder.getRequestFlagCreate();
        this.requestFlagUpdate = builder.getRequestFlagUpdate();
        this.requestFlagManual = builder.isRequestFlagManual();
        this.doLogout = builder.isDoLogout();
        this.expectLoggedIn = builder.isExpectLoggedIn();
        this.refreshableItems = builder.getRefreshableItems();
        this.validator = builder.validator;
        this.redirectUrl = builder.getRedirectUrl();
        this.wireMockServerHost = builder.getWireMockServerHost();
        this.mockCallbackData = builder.getMockSupplementalCallbackData();
        this.clusterIdForSecretsService =
                MoreObjects.firstNonNull(
                        builder.getClusterIdForSecretsService(),
                        NewAgentTestContext.TEST_CLUSTERID);

        this.context =
                new NewAgentTestContext(
                        user,
                        credential,
                        builder.getTransactionsToPrint(),
                        builder.getAppId(),
                        builder.getClusterId(),
                        provider);

        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(context, credential);
    }

    private boolean loadCredentials() {
        if (!loadCredentialsBefore) {
            return false;
        }

        Optional<Credentials> optionalCredential =
                context.getAgentTestServerClient()
                        .loadCredential(provider.getName(), credential.getId());

        optionalCredential.ifPresent(
                c -> {
                    this.credential = c;

                    // Replace the log masker with one that includes the newly loaded credentials
                    this.context.setLogMasker(
                            LogMasker.builder()
                                    .addStringMaskerBuilder(
                                            new CredentialsStringMaskerBuilder(this.credential))
                                    .build());
                });

        return optionalCredential.isPresent();
    }

    private void saveCredentials(Agent agent) {
        if (!saveCredentialsAfter || !(agent instanceof PersistentLogin)) {
            return;
        }

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        // Tell the agent to store data onto the credential (cookies etcetera)
        persistentAgent.persistLoginSession();

        context.getAgentTestServerClient().saveCredential(provider.getName(), credential);
    }

    private RefreshInformationRequest createRefreshInformationRequest() {
        RefreshInformationRequest refreshInformationRequest =
                new RefreshInformationRequest(
                        user,
                        provider,
                        credential,
                        requestFlagManual,
                        requestFlagCreate,
                        requestFlagUpdate);

        refreshInformationRequest.setCallbackUri(redirectUrl);

        return refreshInformationRequest;
    }

    private AgentsServiceConfiguration readConfiguration(String configurationFile)
            throws IOException, ConfigurationException {
        AgentsServiceConfigurationWrapper agentsServiceConfigurationWrapper =
                CONFIGURATION_FACTORY.build(new File(configurationFile));
        return agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();
    }

    private Agent createAgent(CredentialsRequest credentialsRequest) {
        try {
            ManagedTppSecretsServiceClient tppSecretsServiceClient =
                    new TppSecretsServiceClientImpl(
                            configuration.getTppSecretsServiceConfiguration());
            tppSecretsServiceClient.start();
            AgentConfigurationControllerable agentConfigurationController =
                    new AgentConfigurationController(
                            tppSecretsServiceClient,
                            configuration.getIntegrations(),
                            provider,
                            context.getAppId(),
                            clusterIdForSecretsService,
                            credentialsRequest.getCallbackUri());
            context.getLogMasker()
                    .addSensitiveValuesSetObservable(
                            agentConfigurationController.getSecretValuesObservable());
            context.setAgentConfigurationController(agentConfigurationController);

            AgentFactory factory;
            if (wireMockServerHost != null) {
                // Provide AgentFactory with mocked http client and supplemental information.
                factory =
                        new AgentFactory(
                                configuration,
                                new WireMockTinkHttpClientProviderFactory(wireMockServerHost),
                                new MockSupplementalInformationProviderFactory(mockCallbackData),
                                new AgentContextProviderFactoryImpl(),
                                new GeneratedValueProviderImpl(
                                        new ConstantLocalDateTimeSource(),
                                        new ConstantUUIDSource()));

            } else {
                // Provide AgentFactory with 'production' components.
                factory =
                        new AgentFactory(
                                configuration,
                                new NextGenTinkHttpClientProviderFactory(),
                                new SupplementalInformationProviderFactoryImpl(),
                                new AgentContextProviderFactoryImpl(),
                                new GeneratedValueProviderImpl(
                                        new ActualLocalDateTimeSource(), new RandomUUIDSource()));
            }

            Class<? extends Agent> cls = AgentClassFactory.getAgentClass(provider);
            return factory.create(cls, credentialsRequest, context);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isLoggedIn(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        }

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        persistentAgent.loadLoginSession();
        if (!persistentAgent.isLoggedIn()) {
            persistentAgent.clearLoginSession();
            return false;
        }
        return true;
    }

    private void login(Agent agent, CredentialsRequest credentialsRequest) throws Exception {
        if (isLoggedIn(agent)) {
            return;
        }

        if (agent instanceof SubsequentGenerationAgent
                && ((SubsequentGenerationAgent) agent).getAuthenticator()
                        instanceof AuthenticationControllerType) {
            final AuthenticationControllerType manualOrAutoAuth =
                    (AuthenticationControllerType)
                            ((SubsequentGenerationAgent) agent).getAuthenticator();
            final boolean isManual = manualOrAutoAuth.isManualAuthentication(credentialsRequest);
            log.info("Authentication requires user intervention: " + isManual);
        }

        if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login(credentialsRequest.getCredentials());
            return;
        }

        boolean loginSuccessful = agent.login();
        Assert.assertTrue("Agent could not login successfully.", loginSuccessful);
    }

    private boolean keepAlive(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            // Consider it being alive even though it doesn't implement the correct interface.
            return true;
        }

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        boolean alive = persistentAgent.keepAlive();
        if (!alive) {
            persistentAgent.clearLoginSession();
            log.info("Credential is not alive.");
        } else {
            log.info("Credential is alive.");
        }

        return alive;
    }

    private void logout(Agent agent) throws Exception {
        log.info("Logging out credential.");
        agent.logout();
    }

    private void refresh(Agent agent) throws Exception {
        credential.setStatus(CredentialsStatus.UPDATING);

        log.info("Starting refresh.");

        if (agent instanceof DeprecatedRefreshExecutor) {
            log.warn("DeprecatedRefreshExecutor");
            ((DeprecatedRefreshExecutor) agent).refresh();
        } else {
            List<RefreshableItem> sortedItems = RefreshableItem.sort(refreshableItems);
            for (RefreshableItem item : sortedItems) {
                if (item == RefreshableItem.IDENTITY_DATA
                        && agent instanceof RefreshIdentityDataExecutor) {

                    context.cacheIdentityData(
                            ((RefreshIdentityDataExecutor) agent)
                                    .fetchIdentityData()
                                    .getIdentityData());
                } else {
                    RefreshExecutorUtils.executeSegregatedRefresher(agent, item, context);
                }
            }

            if (!RefreshableItem.hasAccounts(sortedItems)) {
                Assert.assertTrue(context.getUpdatedAccounts().isEmpty());
            }

            if (!RefreshableItem.hasTransactions(sortedItems)) {
                Assert.assertTrue(context.getTransactions().isEmpty());
            }

            if (!refreshableItems.contains(RefreshableItem.EINVOICES)) {
                Assert.assertTrue(context.getTransfers().isEmpty());
            }

            if (!refreshableItems.contains(RefreshableItem.TRANSFER_DESTINATIONS)) {
                Assert.assertTrue(context.getTransferDestinationPatterns().isEmpty());
            }
        }

        log.info("Done with refresh.");
    }

    protected void doGenericPaymentBankTransferUKOB(Agent agent, List<Payment> paymentList)
            throws Exception {
        if (agent instanceof PaymentControllerable) {
            log.info("Executing transfer for UkOpenbanking Agent");
            PaymentController paymentController =
                    ((PaymentControllerable) agent)
                            .getPaymentController()
                            .orElseThrow(Exception::new);

            for (Payment payment : paymentList) {
                log.info("Executing bank transfer.");

                PaymentResponse createPaymentResponse =
                        paymentController.create(new PaymentRequest(payment));

                Storage storage = Storage.copyOf(createPaymentResponse.getStorage());

                PaymentMultiStepResponse signPaymentMultiStepResponse =
                        paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

                Map<String, String> map;
                List<Field> fields;
                String nextStep = signPaymentMultiStepResponse.getStep();
                Payment paymentSign = signPaymentMultiStepResponse.getPayment();
                Storage storageSign = signPaymentMultiStepResponse.getStorage();

                while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
                    fields = signPaymentMultiStepResponse.getFields();
                    map = Collections.emptyMap();

                    signPaymentMultiStepResponse =
                            paymentController.sign(
                                    new PaymentMultiStepRequest(
                                            payment,
                                            storage,
                                            nextStep,
                                            fields,
                                            new ArrayList<>(map.values())));
                    nextStep = signPaymentMultiStepResponse.getStep();
                    paymentSign = signPaymentMultiStepResponse.getPayment();
                    storageSign = signPaymentMultiStepResponse.getStorage();
                }

                PaymentResponse paymentResponse =
                        paymentController.fetch(
                                PaymentMultiStepRequest.of(signPaymentMultiStepResponse));
                PaymentStatus statusResult = paymentResponse.getPayment().getStatus();

                Assert.assertTrue(
                        statusResult.equals(PaymentStatus.SIGNED)
                                || statusResult.equals(PaymentStatus.PAID));

                log.info("Done with bank transfer.");
            }
        } else {
            throw new AssertionError(
                    String.format(
                            "%s does not implement a transfer executor interface.",
                            agent.getClass().getSimpleName()));
        }
    }

    protected void doGenericPaymentBankTransfer(Agent agent, List<Payment> paymentList)
            throws Exception {

        if (agent instanceof PaymentControllerable) {
            PaymentController paymentController =
                    ((PaymentControllerable) agent)
                            .getPaymentController()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Agent doesn't implement constructPaymentController method."));

            ArrayList<PaymentRequest> paymentRequests = new ArrayList<>();
            for (Payment payment : paymentList) {
                log.info("Executing bank transfer.");

                PaymentResponse createPaymentResponse =
                        paymentController.create(new PaymentRequest(payment));

                if (paymentController.canFetch()) {
                    PaymentResponse fetchPaymentResponse =
                            paymentController.fetch(PaymentRequest.of(createPaymentResponse));

                    PaymentStatus postCreateStatus = fetchPaymentResponse.getPayment().getStatus();

                    // For payments between user's own accounts signing is rarely (never?) needed,
                    // a payment can then have status signed immediately after the create.
                    Assert.assertTrue(
                            (PaymentStatus.PENDING.equals(postCreateStatus)
                                    || PaymentStatus.SIGNED.equals(postCreateStatus)));

                    paymentRequests.add(PaymentRequest.of(fetchPaymentResponse));
                } else {
                    paymentRequests.add(new PaymentRequest(payment));
                }
            }

            PaymentListResponse paymentListResponse = null;
            if (paymentController.canFetch()) {
                paymentListResponse =
                        paymentController.fetchMultiple(new PaymentListRequest(paymentRequests));
            } else {
                paymentListResponse = PaymentListResponse.of(paymentRequests);
            }

            for (PaymentResponse paymentResponse : paymentListResponse.getPaymentResponseList()) {
                Payment retrievedPayment = paymentResponse.getPayment();
                Storage storage = Storage.copyOf(paymentResponse.getStorage());

                PaymentMultiStepRequest paymentMultiStepRequest =
                        new PaymentMultiStepRequest(
                                retrievedPayment,
                                storage,
                                AuthenticationStepConstants.STEP_INIT,
                                Collections.emptyList(),
                                Collections.emptyList());

                PaymentMultiStepResponse paymentMultiStepResponse =
                        paymentController.sign(paymentMultiStepRequest);

                Map<String, String> map;
                List<Field> fields;
                String nextStep = paymentMultiStepResponse.getStep();
                retrievedPayment = paymentMultiStepResponse.getPayment();
                while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
                    // TODO auth: think about cases other than supplemental info, e.g. bankid,
                    // redirect
                    // etc.
                    if (isSupplementalStep(paymentMultiStepResponse.getStep())) {
                        fields = paymentMultiStepResponse.getFields();
                        map =
                                supplementalInformationController.askSupplementalInformation(
                                        fields.toArray(new Field[fields.size()]));
                    } else {
                        fields = paymentMultiStepResponse.getFields();
                        map = Collections.emptyMap();
                    }

                    paymentMultiStepResponse =
                            paymentController.sign(
                                    new PaymentMultiStepRequest(
                                            retrievedPayment,
                                            storage,
                                            nextStep,
                                            fields,
                                            new ArrayList<>(map.values())));
                    nextStep = paymentMultiStepResponse.getStep();
                    fields = paymentMultiStepResponse.getFields();
                    retrievedPayment = paymentMultiStepResponse.getPayment();
                    storage = paymentMultiStepResponse.getStorage();
                }

                PaymentStatus statusResult = paymentMultiStepResponse.getPayment().getStatus();
                Assert.assertTrue(
                        statusResult.equals(PaymentStatus.SIGNED)
                                || statusResult.equals(PaymentStatus.PAID));

                log.info("Done with bank transfer.");
            }

            // The assert is done here instead of at the beginning of the loop to sign all the
            // pending payments so they will not be present and mess with the test the next time we
            // run it.
            Assert.assertEquals(
                    paymentList.size(), paymentListResponse.getPaymentResponseList().size());
        } else {
            throw new AssertionError(
                    String.format(
                            "%s does not implement a transfer executor interface.",
                            agent.getClass().getSimpleName()));
        }
    }

    private boolean isSupplementalStep(String step) {
        return false;
    }

    private void doBankTransfer(Agent agent, Transfer transfer, boolean isUpdate) throws Exception {
        log.info("Executing bank transfer.");

        if (agent instanceof TransferExecutorNxgen) {
            if (isUpdate) {
                ((TransferExecutorNxgen) agent).update(transfer);
            } else {
                ((TransferExecutorNxgen) agent).execute(transfer);
            }
        } else if (agent instanceof TransferExecutor) {
            if (isUpdate) {
                ((TransferExecutor) agent).update(transfer);
            } else {
                ((TransferExecutor) agent).execute(transfer);
            }

        } else {
            throw new AssertionError(
                    String.format(
                            "%s does not implement a transfer executor interface.",
                            agent.getClass().getSimpleName()));
        }

        log.info("Done with bank transfer.");
    }

    private void initiateCredentials() {
        if (loadCredentials()) {
            // If the credential loaded successful AND the flags were not overridden
            // == non-create/update refresh
            if (requestFlagCreate == null) {
                requestFlagCreate = false;
            }

            if (requestFlagUpdate == null) {
                requestFlagUpdate = false;
            }
        } else {
            // If the credential failed to load (perhaps none previously stored) AND the flags were
            // not overridden
            // == Create new credential
            if (requestFlagCreate == null) {
                requestFlagCreate = true;
            }

            if (requestFlagUpdate == null) {
                requestFlagUpdate = false;
            }
        }
    }

    private void readConfigurationFile() throws ConfigurationException, IOException {
        if (wireMockServerHost != null) {
            configuration = readConfiguration("etc/test.yml");
        } else {
            try {
                configuration = readConfiguration("etc/development.yml");
            } catch (FileNotFoundException e) {
                if (e.getMessage().equals("File etc/development.yml not found")) {
                    final String message =
                            "etc/development.yml missing. Please make a copy of etc/development.template.yml.";
                    throw new IllegalStateException(message);
                }
                throw new IllegalStateException(e);
            }
        }
    }

    public NewAgentTestContext testRefresh(String credentialName) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);

        try {
            login(agent, credentialsRequest);
            refresh(agent);
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.validateFetchedData(validator);
        context.printCollectedData();
        if (!Strings.isNullOrEmpty(credentialName)) {
            CredentialDataDao credentialDataDao = context.dumpCollectedData();
            try {
                dumpTestData(credentialDataDao, credentialName);
            } catch (HttpResponseException | HttpClientException e) {
                System.out.println("Could not dump test data: " + e.getMessage());
            }
        }
        return context;
    }

    private void dumpTestData(CredentialDataDao credentialDataDao, String credentialName) {
        context.getAgentTestServerClient()
                .dumpTestData(provider, credentialName, credentialDataDao);
    }

    public NewAgentTestContext testRefresh() throws Exception {
        return testRefresh("");
    }

    private void printMaskedDebugLog(Agent agent) {
        if (agent instanceof PersistentLogin) {
            final PersistentLogin persistentLoginAgent = (PersistentLogin) agent;
            persistentLoginAgent.persistLoginSession();
        }

        final LogMasker logMasker = context.getLogMasker();
        final String maskedLog = logMasker.mask(context.getLogOutputStream().toString());

        System.out.println();
        System.out.println("===== MASKED DEBUG LOG =====");
        System.out.println(maskedLog);
        System.out.println();
    }

    private void testBankTransfer(Transfer transfer, boolean isUpdate) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);
        try {
            login(agent, credentialsRequest);
            doBankTransfer(agent, transfer, isUpdate);
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    public void testBankTransferUK(Transfer transfer, boolean isUpdate) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);
        try {
            // login(agent, credentialsRequest);
            doBankTransfer(agent, transfer, isUpdate);
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    public void testBankTransfer(Transfer transfer) throws Exception {
        testBankTransfer(transfer, false);
    }

    public void testUpdateTransfer(Transfer transfer) throws Exception {
        testBankTransfer(transfer, true);
    }

    public void testGenericPayment(List<Payment> paymentList) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);

        try {
            login(agent, credentialsRequest);
            if (agent instanceof PaymentControllerable) {
                doGenericPaymentBankTransfer(agent, paymentList);
            } else {
                throw new NotImplementedException(
                        String.format("%s", agent.getAgentClass().getSimpleName()));
            }
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    private List<Payment> createItalianPayments(List<Account> accounts, String desinationAccount) {

        List<Payment> payments = new ArrayList<>();

        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "EUR");
        LocalDate executionDate = LocalDate.now();
        String currency = "EUR";

        payments =
                accounts.stream()
                        .map(
                                a ->
                                        new Payment.Builder()
                                                .withDebtor(
                                                        new Debtor(
                                                                AccountIdentifier.create(
                                                                        AccountIdentifier.Type
                                                                                .SEPA_EUR,
                                                                        a
                                                                                .getAccountNumber()))) // source account
                                                .withCreditor(
                                                        new Creditor(
                                                                AccountIdentifier.create(
                                                                        AccountIdentifier.Type
                                                                                .SEPA_EUR,
                                                                        desinationAccount), // desination account
                                                                "Unknown Person"))
                                                .withExactCurrencyAmount(amount)
                                                .withExecutionDate(executionDate)
                                                .withCurrency(currency)
                                                .withReference(
                                                        new Reference("TRANSFER", "test Tink"))
                                                .withUniqueId(
                                                        RandomUtils.generateRandomHexEncoded(15))
                                                .build())
                        .collect(Collectors.toList());

        return payments;
    }

    public void testGenericPaymentItalia(List<Payment> paymentList) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);

        try {
            // todo: Look into this, currently authentication is done in
            // RedirectDemoPaymentExecutor. Maybe can remove this
            // login(agent, credentialsRequest);
            if (agent instanceof PaymentControllerable) {
                doGenericPaymentBankTransfer(agent, paymentList);
            } else {
                throw new NotImplementedException(
                        String.format("%s", agent.getAgentClass().getSimpleName()));
            }
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    /**
     * Todo: Remove this later
     *
     * @param destinationAccount
     * @throws Exception
     */
    public void testGenericPaymentItalia(String destinationAccount) throws Exception {
        /**
         * The italian flow is special, so we will have to run the AIS flow first and store the
         * aggregated accounts to be used as the source account in the payment flow.
         */
        NewAgentTestContext newAgentTestContext = this.testRefresh();
        List<Account> accounts = newAgentTestContext.getUpdatedAccounts();

        /**
         * Todo: Somehow provide the fetched accounts to frontend, according to Gustaf Fredriksson
         * they use TransferService in tink-backend to fetch source accounts, see @Path("accounts").
         *
         * <p>AccountRepository in tink-backend is responsible for fetching the list of accounts for
         * a specific user. It's an interface and has two implementations, AccountRepositoryV2 and
         * FakeAccountRepository.
         */
        List<Payment> paymentList = createItalianPayments(accounts, destinationAccount);

        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);

        try {
            // todo: Look into this, currently authentication is done in
            // RedirectDemoPaymentExecutor. Maybe can remove this
            // login(agent, credentialsRequest);
            if (agent instanceof PaymentControllerable) {
                doGenericPaymentBankTransfer(agent, paymentList);
            } else {
                throw new NotImplementedException(
                        String.format("%s", agent.getAgentClass().getSimpleName()));
            }
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    public void testGenericPaymentUKOB(List<Payment> paymentList) throws Exception {
        initiateCredentials();
        readConfigurationFile();
        Agent agent = createAgent(createRefreshInformationRequest());
        try {
            // login(agent);
            if (agent instanceof PaymentControllerable) {
                doGenericPaymentBankTransferUKOB(agent, paymentList);
            } else {
                throw new NotImplementedException(
                        String.format("%s", agent.getAgentClass().getSimpleName()));
            }
            if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
                printMaskedDebugLog(agent);
            }
            Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    public Provider getProvider() {
        return provider;
    }

    public static class Builder {
        private static final String DEFAULT_USER_ID = "deadbeefdeadbeefdeadbeefdeadbeef";
        private static final String DEFAULT_CREDENTIAL_ID = "cafebabecafebabecafebabecafebabe";
        private static final String DEFAULT_LOCALE = "sv_SE";

        private final Provider provider;
        private User user = createDefaultUser();
        private Credentials credential = createDefaultCredential();

        private int transactionsToPrint = 32;
        private boolean loadCredentialsBefore = false;
        private boolean saveCredentialsAfter = false;

        // if it should override standard logic
        private Boolean requestFlagCreate = null;
        private Boolean requestFlagUpdate = null;

        private boolean requestFlagManual = true;

        private boolean doLogout = false;
        private boolean expectLoggedIn = true;

        private Set<RefreshableItem> refreshableItems = new HashSet<>();

        private AisValidator validator;

        private String appId = null;
        private String clusterId = null;
        private String redirectUrl;
        private String clusterIdForSecretsService = null;

        private String wireMockServerHost;
        private Map<String, String> mockCallbackData;

        public Builder(String market, String providerName) {
            ProviderConfig marketProviders = readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
        }

        public String getWireMockServerHost() {
            return this.wireMockServerHost;
        }

        private static String escapeMarket(String market) {
            return market.replaceAll("[^a-zA-Z]", "");
        }

        private static ProviderConfig readProvidersConfiguration(String market) {
            String providersFilePath =
                    "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
            File providersFile = new File(providersFilePath);
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(providersFile, ProviderConfig.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private static User createDefaultUser() {
            UserProfile profile = new UserProfile();
            profile.setLocale(DEFAULT_LOCALE);

            User user = new User();
            user.setId(DEFAULT_USER_ID);
            user.setProfile(profile);
            user.setFlags(Lists.newArrayList());

            return user;
        }

        private static Credentials createDefaultCredential() {
            Credentials credential = new Credentials();
            credential.setId(DEFAULT_CREDENTIAL_ID);
            credential.setUserId(DEFAULT_USER_ID);
            credential.setStatus(CredentialsStatus.CREATED);

            return credential;
        }

        public Provider getProvider() {
            return provider;
        }

        public User getUser() {
            return user;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder useWireMockServerHost(final String wireMockServerHost) {
            this.wireMockServerHost = wireMockServerHost;
            return this;
        }

        public Builder withMockSupplementalCallbackData(
                final Map<String, String> mockCallbackData) {
            this.mockCallbackData = mockCallbackData;
            return this;
        }

        public Map<String, String> getMockSupplementalCallbackData() {
            return mockCallbackData;
        }

        public Credentials getCredential() {
            return credential;
        }

        public int getTransactionsToPrint() {
            return transactionsToPrint;
        }

        public boolean isLoadCredentialsBefore() {
            return loadCredentialsBefore;
        }

        public boolean isSaveCredentialsAfter() {
            return saveCredentialsAfter;
        }

        public Boolean getRequestFlagCreate() {
            return requestFlagCreate;
        }

        public Builder setRequestFlagCreate(boolean requestFlagCreate) {
            this.requestFlagCreate = requestFlagCreate;
            return this;
        }

        public Boolean getRequestFlagUpdate() {
            return requestFlagUpdate;
        }

        public Builder setRequestFlagUpdate(boolean requestFlagUpdate) {
            this.requestFlagUpdate = requestFlagUpdate;
            return this;
        }

        public boolean isRequestFlagManual() {
            return requestFlagManual;
        }

        public Builder setRequestFlagManual(boolean requestFlagManual) {
            this.requestFlagManual = requestFlagManual;
            return this;
        }

        public boolean isDoLogout() {
            return doLogout;
        }

        public boolean isExpectLoggedIn() {
            return expectLoggedIn;
        }

        public Set<RefreshableItem> getRefreshableItems() {
            return refreshableItems;
        }

        public Builder setRefreshableItems(Set<RefreshableItem> refreshableItems) {
            this.refreshableItems = refreshableItems;
            return this;
        }

        public String getAppId() {
            return appId;
        }

        public Builder setAppId(final String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setFinancialInstitutionId(final String financialInstitutionId) {
            this.provider.setFinancialInstitutionId(financialInstitutionId);
            return this;
        }

        public Builder setUserLocale(String locale) {
            Preconditions.checkNotNull(this.user, "User not set.");
            Preconditions.checkNotNull(this.user.getProfile(), "User has no profile.");
            this.user.getProfile().setLocale(locale);
            return this;
        }

        public Builder transactionsToPrint(int transactionsToPrint) {
            this.transactionsToPrint = transactionsToPrint;
            return this;
        }

        public Builder loadCredentialsBefore(boolean loadCredentialsBefore) {
            this.loadCredentialsBefore = loadCredentialsBefore;
            return this;
        }

        public Builder saveCredentialsAfter(boolean saveCredentialsAfter) {
            this.saveCredentialsAfter = saveCredentialsAfter;
            return this;
        }

        public Builder doLogout(boolean doLogout) {
            this.doLogout = doLogout;
            return this;
        }

        public Builder expectLoggedIn(boolean expectLoggedIn) {
            this.expectLoggedIn = expectLoggedIn;
            return this;
        }

        public Builder addRefreshableItems(RefreshableItem... items) {
            this.refreshableItems.addAll(Arrays.asList(items));
            return this;
        }

        public Builder setCredentialId(String credentialId) {
            credential.setId(credentialId);
            return this;
        }

        public Builder setCredentialFields(Map<String, String> fields) {
            this.credential.setFields(fields);
            return this;
        }

        public Builder addCredentialField(String key, String value) {
            this.credential.setField(key, value);
            return this;
        }

        public Builder addCredentialField(Field.Key key, String value) {
            return addCredentialField(key.getFieldKey(), value);
        }

        /** Inject a custom validator of AIS data. */
        public Builder setValidator(final AisValidator validator) {
            this.validator = validator;
            return this;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public Builder setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public String getClusterId() {
            return clusterId;
        }

        public Builder setClusterId(String clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public String getClusterIdForSecretsService() {
            return clusterIdForSecretsService;
        }

        public Builder setClusterIdForSecretsService(String clusterIdForSecretsService) {
            this.clusterIdForSecretsService = clusterIdForSecretsService;
            return this;
        }

        public AgentIntegrationTest build() {
            if (refreshableItems.isEmpty()) {
                refreshableItems.addAll(
                        RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL));
            }

            Preconditions.checkNotNull(provider, "Provider was not set.");
            Preconditions.checkNotNull(credential, "Credential was not set.");
            credential.setProviderName(provider.getName());
            credential.setType(provider.getCredentialsType());

            if (validator == null) {
                validator = ValidatorFactory.getExtensiveValidator();
            }

            return new AgentIntegrationTest(this);
        }
    }
}
