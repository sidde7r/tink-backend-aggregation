package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.AgentClassFactory;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactory;
import se.tink.backend.aggregation.agents.framework.AgentTestServerClient;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;
import se.tink.backend.aggregation.agents.module.loader.PackageModuleLoader;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor.ProgressiveLoginExecutor;
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
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public final class TargobankAgentIntegrationTest extends AbstractConfigurationBase {
    private static final Logger log = LoggerFactory.getLogger(TargobankAgentIntegrationTest.class);
    private final Provider provider;
    private final User user;
    private final boolean loadCredentialsBefore;
    private final boolean saveCredentialsAfter;
    private final boolean requestFlagManual;
    private final boolean doLogout;
    private final boolean expectLoggedIn;
    private final NewAgentTestContext context;
    private final SupplementalInformationController supplementalInformationController;
    private Credentials credential;
    private Boolean requestFlagCreate;
    private Boolean requestFlagUpdate;
    private final AgentTestServerClient agentTestServerClient;

    private TargobankAgentIntegrationTest(TargobankAgentIntegrationTest.Builder builder) {
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
        this.context =
                new NewAgentTestContext(
                        this.user,
                        this.credential,
                        builder.getTransactionsToPrint(),
                        "",
                        "",
                        provider);
        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(this.context, this.credential);
        agentTestServerClient = AgentTestServerClient.getInstance();
    }

    private boolean loadCredentials() {
        if (!this.loadCredentialsBefore) {
            return false;
        } else {
            Optional<Credentials> optionalCredential =
                    agentTestServerClient.loadCredential(
                            this.provider.getName(), this.credential.getId());
            optionalCredential.ifPresent(credentials -> this.credential = credentials);
            return optionalCredential.isPresent();
        }
    }

    private void saveCredentials(Agent agent) {
        if (this.saveCredentialsAfter && agent instanceof PersistentLogin) {
            PersistentLogin persistentAgent = (PersistentLogin) agent;
            persistentAgent.persistLoginSession();
            agentTestServerClient.saveCredential(this.provider.getName(), this.credential);
        }
    }

    private RefreshInformationRequest createRefreshInformationRequest() {
        return new RefreshInformationRequest(
                this.user,
                this.provider,
                this.credential,
                this.requestFlagManual,
                this.requestFlagCreate,
                this.requestFlagUpdate);
    }

    private Agent createAgent(CredentialsRequest credentialsRequest) {
        try {
            AgentsServiceConfigurationWrapper agentsServiceConfigurationWrapper =
                    this.CONFIGURATION_FACTORY.build(new File("etc/development.yml"));
            this.configuration = agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();

            // Provide AgentFactory with 'production' components.
            AgentFactory factory =
                    new AgentFactory(
                            new AgentPackageModuleFactory(new PackageModuleLoader()),
                            configuration);
            Class<? extends Agent> cls = AgentClassFactory.getAgentClass(this.provider);
            return factory.create(cls, credentialsRequest, this.context);
        } catch (FileNotFoundException e) {
            if (e.getMessage().equals("File etc/development.yml not found")) {
                String message =
                        "etc/development.yml missing. Please make a copy of etc/development.template.yml.";
                throw new IllegalStateException(
                        "etc/development.yml missing. Please make a copy of etc/development.template.yml.");
            } else {
                throw new IllegalStateException(e);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isLoggedIn(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        } else {
            PersistentLogin persistentAgent = (PersistentLogin) agent;
            persistentAgent.loadLoginSession();
            if (!persistentAgent.isLoggedIn()) {
                persistentAgent.clearLoginSession();
                return false;
            } else {
                return true;
            }
        }
    }

    private void login(Agent agent) throws Exception {
        if (!this.isLoggedIn(agent)) {
            if (agent instanceof ProgressiveAuthAgent) {
                ProgressiveLoginExecutor executor =
                        new ProgressiveLoginExecutor(
                                this.supplementalInformationController,
                                (ProgressiveAuthAgent) agent);
                executor.login(credential);
            } else {
                boolean loginSuccessful = agent.login();
                Assert.assertTrue("Agent could not login successfully.", loginSuccessful);
            }
        }
    }

    private boolean keepAlive(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return true;
        } else {
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
    }

    private void logout(Agent agent) throws Exception {
        log.info("Logging out credential.");
        agent.logout();
    }

    private void doGenericPaymentBankTransfer(Agent agent, List<Payment> paymentList)
            throws Exception {

        if (agent instanceof PaymentControllerable) {
            PaymentController paymentController =
                    ((PaymentControllerable) agent)
                            .getPaymentController()
                            .orElseThrow(Exception::new);
            ArrayList<PaymentRequest> paymentRequests = new ArrayList<>();

            for (Payment payment : paymentList) {
                log.info("Executing bank transfer.");
                PaymentResponse createPaymentResponse =
                        paymentController.create(new PaymentRequest(payment));
                PaymentResponse fetchPaymentResponse =
                        paymentController.fetch(PaymentRequest.of(createPaymentResponse));
                Assert.assertEquals(
                        PaymentStatus.CREATED, fetchPaymentResponse.getPayment().getStatus());
                paymentRequests.add(PaymentRequest.of(fetchPaymentResponse));
            }

            PaymentListResponse paymentListResponse =
                    paymentController.fetchMultiple(new PaymentListRequest(paymentRequests));

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
                    if (this.isSupplementalStep(paymentMultiStepResponse.getStep())) {
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
                    (long) paymentList.size(),
                    (long) paymentListResponse.getPaymentResponseList().size());
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

    private void initiateCredentials() {
        if (this.loadCredentials()) {
            if (this.requestFlagCreate == null) {
                this.requestFlagCreate = false;
            }

            if (this.requestFlagUpdate == null) {
                this.requestFlagUpdate = false;
            }
        } else {
            if (this.requestFlagCreate == null) {
                this.requestFlagCreate = true;
            }

            if (this.requestFlagUpdate == null) {
                this.requestFlagUpdate = false;
            }
        }
    }

    public void testGenericPayment(List<Payment> paymentList) throws Exception {
        this.initiateCredentials();
        Agent agent = this.createAgent(this.createRefreshInformationRequest());

        try {
            this.login(agent);
            if (!(agent instanceof PaymentControllerable)) {
                throw new NotImplementedException(
                        String.format("%s", agent.getAgentClass().getSimpleName()));
            }

            this.doGenericPaymentBankTransfer(agent, paymentList);
            Assert.assertTrue(
                    "Expected to be logged in.", !this.expectLoggedIn || this.keepAlive(agent));
            if (this.doLogout) {
                this.logout(agent);
            }
        } finally {
            this.saveCredentials(agent);
        }

        this.context.printCollectedData();
    }

    public static class Builder {
        private final Provider provider;
        private User user = createDefaultUser();
        private Credentials credential = createDefaultCredential();
        private int transactionsToPrint = 32;
        private boolean loadCredentialsBefore = false;
        private boolean saveCredentialsAfter = false;
        private Boolean requestFlagCreate = null;
        private Boolean requestFlagUpdate = null;
        private boolean requestFlagManual = true;
        private boolean doLogout = false;
        private boolean expectLoggedIn = true;
        private Set<RefreshableItem> refreshableItems = new HashSet<>();
        private AisValidator validator;

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
                    "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
            File providersFile = new File(providersFilePath);
            ObjectMapper mapper = new ObjectMapper();

            try {
                return mapper.readValue(providersFile, ProviderConfig.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private static User createDefaultUser() {
            UserProfile profile = new UserProfile();
            profile.setLocale("sv_SE");
            User user = new User();
            user.setId("deadbeefdeadbeefdeadbeefdeadbeef");
            user.setProfile(profile);
            user.setFlags(Lists.newArrayList());
            return user;
        }

        private static Credentials createDefaultCredential() {
            Credentials credential = new Credentials();
            credential.setId("cafebabecafebabecafebabecafebabe");
            credential.setUserId("deadbeefdeadbeefdeadbeefdeadbeef");
            credential.setStatus(CredentialsStatus.CREATED);
            return credential;
        }

        public Provider getProvider() {
            return this.provider;
        }

        public User getUser() {
            return this.user;
        }

        public Credentials getCredential() {
            return this.credential;
        }

        public int getTransactionsToPrint() {
            return this.transactionsToPrint;
        }

        public boolean isLoadCredentialsBefore() {
            return this.loadCredentialsBefore;
        }

        public boolean isSaveCredentialsAfter() {
            return this.saveCredentialsAfter;
        }

        public Boolean getRequestFlagCreate() {
            return this.requestFlagCreate;
        }

        public Boolean getRequestFlagUpdate() {
            return this.requestFlagUpdate;
        }

        public boolean isRequestFlagManual() {
            return this.requestFlagManual;
        }

        public boolean isDoLogout() {
            return this.doLogout;
        }

        public boolean isExpectLoggedIn() {
            return this.expectLoggedIn;
        }

        public TargobankAgentIntegrationTest.Builder loadCredentialsBefore(
                boolean loadCredentialsBefore) {
            this.loadCredentialsBefore = loadCredentialsBefore;
            return this;
        }

        public TargobankAgentIntegrationTest.Builder saveCredentialsAfter(
                boolean saveCredentialsAfter) {
            this.saveCredentialsAfter = saveCredentialsAfter;
            return this;
        }

        public TargobankAgentIntegrationTest.Builder expectLoggedIn(boolean expectLoggedIn) {
            this.expectLoggedIn = expectLoggedIn;
            return this;
        }

        public TargobankAgentIntegrationTest.Builder addCredentialField(String key, String value) {
            this.credential.setField(key, value);
            return this;
        }

        public TargobankAgentIntegrationTest build() {
            if (this.refreshableItems.isEmpty()) {
                this.refreshableItems.addAll(
                        RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL));
            }

            Preconditions.checkNotNull(this.provider, "Provider was not set.");
            Preconditions.checkNotNull(this.credential, "Credential was not set.");
            this.credential.setProviderName(this.provider.getName());
            this.credential.setType(this.provider.getCredentialsType());
            if (this.validator == null) {
                this.validator = ValidatorFactory.getExtensiveValidator();
            }

            return new TargobankAgentIntegrationTest(this);
        }
    }
}
