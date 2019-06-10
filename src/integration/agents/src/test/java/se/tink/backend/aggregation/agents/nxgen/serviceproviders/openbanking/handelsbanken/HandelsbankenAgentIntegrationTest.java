//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentClassFactory;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.framework.AgentTestServerClient;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.annotations.ProgressiveAuth;
import se.tink.backend.aggregation.configuration.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
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

public final class HandelsbankenAgentIntegrationTest extends AbstractConfigurationBase {
    private static final Logger log = LoggerFactory.getLogger(AbstractAgentTest.class);
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

    private HandelsbankenAgentIntegrationTest(HandelsbankenAgentIntegrationTest.Builder builder) {
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
                        this.user, this.credential, builder.getTransactionsToPrint());
        this.supplementalInformationController =
                new SupplementalInformationController(this.context, this.credential);
    }

    private boolean loadCredentials() {
        if (!this.loadCredentialsBefore) {
            return false;
        } else {
            Optional<Credentials> optionalCredential =
                    AgentTestServerClient.loadCredential(
                            this.provider.getName(), this.credential.getId());
            optionalCredential.ifPresent(
                    (c) -> {
                        this.credential = c;
                    });
            return optionalCredential.isPresent();
        }
    }

    private void saveCredentials(Agent agent) {
        if (this.saveCredentialsAfter && agent instanceof PersistentLogin) {
            PersistentLogin persistentAgent = (PersistentLogin) agent;
            persistentAgent.persistLoginSession();
            AgentTestServerClient.saveCredential(this.provider.getName(), this.credential);
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
                    (AgentsServiceConfigurationWrapper)
                            this.CONFIGURATION_FACTORY.build(new File("etc/development.yml"));
            this.configuration = agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();
            AgentFactory factory = new AgentFactory(this.configuration);
            Class<? extends Agent> cls = AgentClassFactory.getAgentClass(this.provider);
            return factory.create(cls, credentialsRequest, this.context);
        } catch (FileNotFoundException var5) {
            if (var5.getMessage().equals("File etc/development.yml not found")) {
                String message =
                        "etc/development.yml missing. Please make a copy of etc/development.template.yml.";
                throw new IllegalStateException(
                        "etc/development.yml missing. Please make a copy of etc/development.template.yml.");
            } else {
                throw new IllegalStateException(var5);
            }
        } catch (Exception var6) {
            throw new IllegalStateException(var6);
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

    private void progressiveLogin(final Agent agent) throws Exception {
        ProgressiveAuthAgent progressiveAgent = (ProgressiveAuthAgent) agent;

        Map map;
        for (SteppableAuthenticationResponse response =
                        progressiveAgent.login(SteppableAuthenticationRequest.initialRequest());
                response.getStep().isPresent();
                response =
                        progressiveAgent.login(
                                SteppableAuthenticationRequest.subsequentRequest(
                                        (Class) response.getStep().get(),
                                        new ArrayList(map.values())))) {
            List<Field> fields = response.getFields();
            map =
                    this.supplementalInformationController.askSupplementalInformation(
                            (Field[]) fields.toArray(new Field[fields.size()]));
        }
    }

    private void login(Agent agent) throws Exception {
        if (!this.isLoggedIn(agent)) {
            if (agent.getAgentClass().getAnnotation(ProgressiveAuth.class) != null) {
                this.progressiveLogin(agent);
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
        if (!(agent instanceof SubsequentGenerationAgent)) {
            throw new AssertionError(
                    String.format(
                            "%s does not implement a transfer executor interface.",
                            agent.getClass().getSimpleName()));
        } else {
            PaymentController paymentController =
                    (PaymentController)
                            ((SubsequentGenerationAgent) agent)
                                    .constructPaymentController()
                                    .orElseThrow(Exception::new);
            ArrayList<PaymentRequest> paymentRequests = new ArrayList();
            Iterator var5 = paymentList.iterator();

            PaymentResponse paymentResponse;
            while (var5.hasNext()) {
                Payment payment = (Payment) var5.next();
                log.info("Executing bank transfer.");
                paymentResponse = paymentController.create(new PaymentRequest(payment));
                PaymentResponse fetchPaymentResponse =
                        paymentController.fetch(PaymentRequest.of(paymentResponse));
                Assert.assertEquals(
                        PaymentStatus.PAID, fetchPaymentResponse.getPayment().getStatus());
                paymentRequests.add(PaymentRequest.of(fetchPaymentResponse));
            }

            PaymentListResponse paymentListResponse =
                    paymentController.fetchMultiple(new PaymentListRequest(paymentRequests));
            Iterator var17 = paymentListResponse.getPaymentResponseList().iterator();

            while (var17.hasNext()) {
                paymentResponse = (PaymentResponse) var17.next();
                Payment retrievedPayment = paymentResponse.getPayment();
                Storage storage = Storage.copyOf(paymentResponse.getStorage());
                PaymentMultiStepRequest paymentMultiStepRequest =
                        new PaymentMultiStepRequest(
                                retrievedPayment,
                                storage,
                                "init",
                                Collections.emptyList(),
                                Collections.emptyList());
                PaymentMultiStepResponse paymentMultiStepResponse =
                        paymentController.sign(paymentMultiStepRequest);
                String nextStep = paymentMultiStepResponse.getStep();

                for (retrievedPayment = paymentMultiStepResponse.getPayment();
                        !"finalize".equals(nextStep);
                        storage = paymentMultiStepResponse.getStorage()) {
                    Map map;
                    List fields;
                    if (this.isSupplementalStep(paymentMultiStepResponse.getStep())) {
                        fields = paymentMultiStepResponse.getFields();
                        map =
                                this.supplementalInformationController.askSupplementalInformation(
                                        (Field[]) fields.toArray(new Field[fields.size()]));
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
                                            new ArrayList(map.values())));
                    nextStep = paymentMultiStepResponse.getStep();
                    fields = paymentMultiStepResponse.getFields();
                    retrievedPayment = paymentMultiStepResponse.getPayment();
                }

                PaymentStatus statusResult = paymentMultiStepResponse.getPayment().getStatus();
                Assert.assertTrue(
                        statusResult.equals(PaymentStatus.SIGNED)
                                || statusResult.equals(PaymentStatus.PAID));
                log.info("Done with bank transfer.");
            }

            Assert.assertEquals(
                    (long) paymentList.size(),
                    (long) paymentListResponse.getPaymentResponseList().size());
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
            if (!(agent instanceof SubsequentGenerationAgent)) {
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
        private Set<RefreshableItem> refreshableItems = new HashSet();
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
                return (ProviderConfig) mapper.readValue(providersFile, ProviderConfig.class);
            } catch (IOException var5) {
                throw new IllegalStateException(var5);
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

        public HandelsbankenAgentIntegrationTest.Builder loadCredentialsBefore(
                boolean loadCredentialsBefore) {
            this.loadCredentialsBefore = loadCredentialsBefore;
            return this;
        }

        public HandelsbankenAgentIntegrationTest.Builder saveCredentialsAfter(
                boolean saveCredentialsAfter) {
            this.saveCredentialsAfter = saveCredentialsAfter;
            return this;
        }

        public HandelsbankenAgentIntegrationTest.Builder expectLoggedIn(boolean expectLoggedIn) {
            this.expectLoggedIn = expectLoggedIn;
            return this;
        }

        public HandelsbankenAgentIntegrationTest.Builder addCredentialField(
                String key, String value) {
            this.credential.setField(key, value);
            return this;
        }

        public HandelsbankenAgentIntegrationTest build() {
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

            return new HandelsbankenAgentIntegrationTest(this);
        }
    }
}
