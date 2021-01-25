package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.CreateBeneficiaryControllerable;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.contractproducer.ContractProducer;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.dao.CredentialDataDao;
import se.tink.backend.aggregation.agents.framework.modules.production.AgentIntegrationTestModule;
import se.tink.backend.aggregation.agents.framework.testserverclient.AgentTestServerClient;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
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
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClientImpl;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class AgentIntegrationTest extends AbstractConfigurationBase {

    private static final Logger log = LoggerFactory.getLogger(AgentIntegrationTest.class);

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
    private final String originatingUserIp;
    // if it should override standard logic (Todo: find a better way to implement this!)
    private Boolean requestFlagCreate;
    private Boolean requestFlagUpdate;
    private Boolean dumpContentForContractFile;

    protected AgentIntegrationTest(Builder builder) {
        this.provider = builder.getProvider();
        this.user = builder.getUser();
        this.credential = builder.getCredential();
        this.originatingUserIp = builder.getOriginatingUserIp();
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
        this.dumpContentForContractFile = builder.isDumpContentForContractFile();

        this.clusterIdForSecretsService =
                MoreObjects.firstNonNull(
                        builder.getClusterIdForSecretsService(),
                        NewAgentTestContext.TEST_CLUSTERID);

        this.context =
                new NewAgentTestContext(
                        user,
                        credential,
                        new AgentTestServerSupplementalRequester(
                                credential, AgentTestServerClient.getInstance()),
                        builder.getTransactionsToPrint(),
                        builder.getAppId(),
                        builder.getClusterId(),
                        provider);

        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(context, credential, null);
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

                    // Replace credential object in context as well
                    this.context.setCredential(c);

                    // Replace the log masker with one that includes the newly loaded credentials
                    this.context.setLogMasker(new FakeLogMasker());
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
                RefreshInformationRequest.builder()
                        .user(user)
                        .provider(provider)
                        .credentials(credential)
                        .originatingUserIp(originatingUserIp)
                        .manual(requestFlagManual)
                        .create(requestFlagCreate)
                        .update(requestFlagUpdate)
                        .forceAuthenticate(false)
                        .build();

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

            final Injector injector =
                    Guice.createInjector(new AgentIntegrationTestModule(configuration));
            final AgentFactory factory = injector.getInstance(AgentFactory.class);

            return factory.create(credentialsRequest, context);
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

        if (agent instanceof AgentPlatformAuthenticator) {
            new AgentPlatformAuthenticationExecutor()
                    .processAuthentication(
                            agent, credentialsRequest, supplementalInformationController);
            return;
        }

        if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login(credentialsRequest);
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

            if (!refreshableItems.contains(RefreshableItem.TRANSFER_DESTINATIONS)
                    && !refreshableItems.contains(RefreshableItem.LIST_BENEFICIARIES)) {
                Assert.assertTrue(context.getTransferDestinationPatterns().isEmpty());
            }
        }

        log.info("Done with refresh.");
    }

    protected void doGenericPaymentBankTransferUKOB(Agent agent, Payment payment) throws Exception {
        if (!(agent instanceof TypedPaymentControllerable)) {
            throw new AssertionError(
                    String.format(
                            "%s does not implement a transfer executor interface.",
                            agent.getClass().getSimpleName()));
        }

        log.info("Executing transfer for UkOpenbanking Agent");
        PaymentController paymentController =
                ((TypedPaymentControllerable) agent)
                        .getPaymentController(payment)
                        .orElseThrow(Exception::new);

        log.info("Executing bank transfer.");

        PaymentResponse createPaymentResponse =
                paymentController.create(new PaymentRequest(payment));

        Storage storage = Storage.copyOf(createPaymentResponse.getStorage());

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        Map<String, String> map;
        List<Field> fields;
        String nextStep = signPaymentMultiStepResponse.getStep();

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

            PaymentResponse paymentResponse =
                    paymentController.fetch(
                            PaymentMultiStepRequest.of(signPaymentMultiStepResponse));
            PaymentStatus statusResult = paymentResponse.getPayment().getStatus();

            Assert.assertTrue(
                    statusResult.equals(PaymentStatus.SIGNED)
                            || statusResult.equals(PaymentStatus.PAID));

            log.info("Done with bank transfer.");
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

            PaymentListResponse paymentListResponse;
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
                    fields = paymentMultiStepResponse.getFields();
                    map = Collections.emptyMap();

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

    protected void doTinkLinkPaymentBankTransfer(
            PaymentController paymentController, Payment payment) throws Exception {
        log.info("Executing bank transfer.");

        PaymentResponse createPaymentResponse =
                paymentController.create(new PaymentRequest(payment, originatingUserIp));

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        Map<String, String> map;
        List<Field> fields;
        String nextStep = signPaymentMultiStepResponse.getStep();
        Payment paymentFromResponse = signPaymentMultiStepResponse.getPayment();
        Storage storage = signPaymentMultiStepResponse.getStorage();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signPaymentMultiStepResponse.getFields();
            map = Collections.emptyMap();

            signPaymentMultiStepResponse =
                    paymentController.sign(
                            new PaymentMultiStepRequest(
                                    paymentFromResponse,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signPaymentMultiStepResponse.getStep();
            paymentFromResponse = signPaymentMultiStepResponse.getPayment();
            storage = signPaymentMultiStepResponse.getStorage();
        }

        PaymentStatus statusResult = paymentFromResponse.getStatus();
        Assert.assertEquals(statusResult, PaymentStatus.SIGNED);

        log.info("Done with bank transfer.");
    }

    private void doBankTransfer(Agent agent, Transfer transfer) throws Exception {
        log.info("Executing bank transfer.");

        if (agent instanceof TransferExecutorNxgen) {
            ((TransferExecutorNxgen) agent).execute(transfer);
        } else if (agent instanceof TransferExecutor) {
            ((TransferExecutor) agent).execute(transfer);
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
        try {
            configuration = readConfiguration("etc/development.yml");
        } catch (FileNotFoundException e) {
            if (e.getMessage().equals("File etc/development.yml not found")) {
                final String message =
                        "etc/development.yml missing. Please make a copy of etc/test.yml.";
                throw new IllegalStateException(message);
            }
            throw new IllegalStateException(e);
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

        context.validateCredentials();
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

        if (dumpContentForContractFile) {
            ContractProducer contractProducer = new ContractProducer();
            log.info(
                    "This is the content for building the contract file : \n"
                            + contractProducer.produceFromContext(context));
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

        final LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(this.credential))
                        .build();
        final String maskedLog = logMasker.mask(context.getLogOutputStream().toString());

        System.out.println();
        System.out.println("===== MASKED DEBUG LOG =====");
        System.out.println(maskedLog);
        System.out.println();
    }

    public void testBankTransfer(Transfer transfer) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);
        try {
            login(agent, credentialsRequest);
            doBankTransfer(agent, transfer);
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

    public void testBankTransferUK(Transfer transfer) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);
        try {
            // login(agent, credentialsRequest);
            doBankTransfer(agent, transfer);
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

    public void testTinkLinkPayment(Payment payment) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);

        try {
            if (agent instanceof TypedPaymentControllerable) {
                PaymentController paymentController =
                        ((TypedPaymentControllerable) agent)
                                .getPaymentController(payment)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Agent doesn't implement constructPaymentController method."));

                doTinkLinkPaymentBankTransfer(paymentController, payment);
            } else if (agent instanceof PaymentControllerable) {
                PaymentController paymentController =
                        ((PaymentControllerable) agent)
                                .getPaymentController()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Agent doesn't implement constructPaymentController method."));

                doTinkLinkPaymentBankTransfer(paymentController, payment);
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

    public void testCreateBeneficiary(CreateBeneficiary createBeneficiary) throws Exception {
        initiateCredentials();
        RefreshInformationRequest credentialsRequest = createRefreshInformationRequest();
        readConfigurationFile();
        Agent agent = createAgent(credentialsRequest);
        try {
            login(agent, credentialsRequest);
            if (agent instanceof CreateBeneficiaryControllerable) {
                log.info("Adding beneficiary.");
                CreateBeneficiaryController createBeneficiaryController =
                        ((CreateBeneficiaryControllerable) agent)
                                .getCreateBeneficiaryController()
                                .orElseThrow(
                                        () ->
                                                new NotImplementedException(
                                                        "Agent does not implement constructAddBeneficiaryController method."));

                CreateBeneficiaryResponse beneficiaryResponse =
                        createBeneficiaryController.createBeneficiary(
                                new CreateBeneficiaryRequest(createBeneficiary));
                CreateBeneficiary retrievedBeneficiary = beneficiaryResponse.getBeneficiary();
                Storage storage = Storage.copyOf(beneficiaryResponse.getStorage());
                CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest =
                        new CreateBeneficiaryMultiStepRequest(
                                retrievedBeneficiary,
                                storage,
                                AuthenticationStepConstants.STEP_INIT,
                                Collections.emptyList(),
                                Collections.emptyList());
                CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                        createBeneficiaryController.sign(createBeneficiaryMultiStepRequest);
                Map<String, String> map;
                List<Field> fields;
                String nextStep = createBeneficiaryMultiStepResponse.getStep();
                retrievedBeneficiary = createBeneficiaryMultiStepResponse.getBeneficiary();
                while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
                    fields = createBeneficiaryMultiStepResponse.getFields();
                    map = Collections.emptyMap();

                    createBeneficiaryMultiStepResponse =
                            createBeneficiaryController.sign(
                                    new CreateBeneficiaryMultiStepRequest(
                                            retrievedBeneficiary,
                                            storage,
                                            nextStep,
                                            fields,
                                            new ArrayList<>(map.values())));
                    nextStep = createBeneficiaryMultiStepResponse.getStep();
                    fields = createBeneficiaryMultiStepResponse.getFields();
                    retrievedBeneficiary = createBeneficiaryMultiStepResponse.getBeneficiary();
                    storage = createBeneficiaryMultiStepResponse.getStorage();
                }

                CreateBeneficiaryStatus statusResult =
                        createBeneficiaryMultiStepResponse.getBeneficiary().getStatus();
                Assert.assertEquals(statusResult, CreateBeneficiaryStatus.CREATED);
                Beneficiary beneficiary = createBeneficiary.getBeneficiary();

                String ownerAccountNumber =
                        Strings.isNullOrEmpty(createBeneficiary.getOwnerAccountNumber())
                                ? ""
                                : StringUtils.overlay(
                                        createBeneficiary.getOwnerAccountNumber(),
                                        StringUtils.repeat(
                                                '*',
                                                createBeneficiary.getOwnerAccountNumber().length()
                                                        - 4),
                                        0,
                                        beneficiary.getAccountNumber().length() - 4);
                String accountNumber =
                        Strings.isNullOrEmpty(beneficiary.getAccountNumber())
                                ? ""
                                : StringUtils.overlay(
                                        beneficiary.getAccountNumber(),
                                        StringUtils.repeat(
                                                '*', beneficiary.getAccountNumber().length() - 4),
                                        0,
                                        beneficiary.getAccountNumber().length() - 4);
                log.info(
                        "Done with adding beneficiary, name: {}, type: {}, account number: {}, owner account number: {}",
                        beneficiary.getName(),
                        beneficiary.getAccountNumberType(),
                        accountNumber,
                        ownerAccountNumber);

            } else {
                throw new NotImplementedException(agent.getAgentClass().getSimpleName());
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
    }

    public void testGenericPaymentForRedirect(List<Payment> paymentList) throws Exception {
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

    public void testGenericPaymentUKOB(Payment payment) throws Exception {
        initiateCredentials();
        readConfigurationFile();
        Agent agent = createAgent(createRefreshInformationRequest());

        doGenericPaymentBankTransferUKOB(agent, payment);

        if (configuration.getTestConfiguration().isDebugOutputEnabled()) {
            printMaskedDebugLog(agent);
        }
        Assert.assertTrue("Expected to be logged in.", !expectLoggedIn || keepAlive(agent));

        if (doLogout) {
            logout(agent);
        }

        saveCredentials(agent);

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
        private String originatingUserIp = "127.0.0.1";

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

        private boolean dumpContentForContractFile = false;

        public Builder(String market, String providerName) {
            ProviderConfig marketProviders = readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
        }

        private static String escapeMarket(String market) {
            return market.replaceAll("[^a-zA-Z]", "");
        }

        private static ProviderConfig readProvidersConfiguration(String market) {
            String providersFilePath =
                    "external/tink_backend/src/provider_configuration/data/seeding/providers-"
                            + escapeMarket(market).toLowerCase()
                            + ".json";
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

        public Builder dumpContentForContractFile() {
            this.dumpContentForContractFile = true;
            return this;
        }

        public boolean isDumpContentForContractFile() {
            return this.dumpContentForContractFile;
        }

        public String getOriginatingUserIp() {
            return originatingUserIp;
        }

        public Builder setOriginatingUserIp(String originatingUserIp) {
            this.originatingUserIp = originatingUserIp;
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
