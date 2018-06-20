package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.AbstractConfigurationBase;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.rpc.UserProfile;

public class AgentIntegrationTest extends AbstractConfigurationBase {
    private static final Logger log = LoggerFactory.getLogger(AbstractAgentTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Provider provider;
    private final User user;
    private Credentials credential;

    private final boolean loadCredentialsBefore;
    private final boolean saveCredentialsAfter;

    // if it should override standard logic (Todo: find a better way to implement this!)
    private Boolean requestFlagCreate;
    private Boolean requestFlagUpdate;
    private final boolean requestFlagManual;

    private final boolean doLogout;

    private final Set<RefreshableItem> refreshableItems;

    private final NewAgentTestContext context;


    private AgentIntegrationTest(Builder builder) {
        this.provider = builder.getProvider();
        this.user = builder.getUser();
        this.credential = builder.getCredential();
        this.loadCredentialsBefore = builder.isLoadCredentialsBefore();
        this.saveCredentialsAfter = builder.isSaveCredentialsAfter();
        this.requestFlagCreate = builder.getRequestFlagCreate();
        this.requestFlagUpdate = builder.getRequestFlagUpdate();
        this.requestFlagManual = builder.isRequestFlagManual();
        this.doLogout = builder.isDoLogout();
        this.refreshableItems = builder.getRefreshableItems();

        this.context = new NewAgentTestContext(user, credential, builder.getTransactionsToPrint());
    }

    private boolean loadCredentials() {
        if (!loadCredentialsBefore) {
            return false;
        }

        Optional<Credentials> optionalCredential = AgentTestServerClient.loadCredential(provider.getName(),
                credential.getId());

        optionalCredential.ifPresent(c -> this.credential = c);

        return optionalCredential.isPresent();
    }

    private void saveCredentials(Agent agent) {
        if (!saveCredentialsAfter || !(agent instanceof PersistentLogin)) {
            return;
        }

        PersistentLogin persistentAgent = PersistentLogin.class.cast(agent);

        // Tell the agent to store data onto the credential (cookies etcetera)
        persistentAgent.persistLoginSession();

        AgentTestServerClient.saveCredential(provider.getName(), credential);
    }

    private RefreshInformationRequest createRefreshInformationRequest() {
        return new RefreshInformationRequest(user, provider, credential, requestFlagManual, requestFlagCreate,
                requestFlagUpdate);
    }

    private Agent createAgent(CredentialsRequest credentialsRequest) {
        try {
            configuration = CONFIGURATION_FACTORY.build(new File("etc/development.yml"));
            AgentFactory factory = new AgentFactory(configuration);

            Class<? extends Agent> cls = AgentFactory.getAgentClass(provider);
            return factory.create(cls, credentialsRequest, context);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isLoggedIn(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        }

        PersistentLogin persistentAgent = PersistentLogin.class.cast(agent);

        persistentAgent.loadLoginSession();
        if (!persistentAgent.isLoggedIn()) {
            persistentAgent.clearLoginSession();
            return false;
        }
        return true;
    }

    private void login(Agent agent) throws Exception {
        if (isLoggedIn(agent)) {
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
            ((DeprecatedRefreshExecutor) agent).refresh();

            // process everything
            context.processAccounts();
            context.processTransactions();
            context.processEinvoices();
            context.processTransferDestinationPatterns();

        } else if (agent instanceof RefreshableItemExecutor) {
            RefreshableItemExecutor refreshExecutor = (RefreshableItemExecutor) agent;

            List<RefreshableItem> sortedItems = RefreshableItem.sort(refreshableItems);
            for (RefreshableItem item : sortedItems) {
                refreshExecutor.refresh(item);
            }

            if (RefreshableItem.hasAccounts(sortedItems)) {
                context.processAccounts();
            } else {
                Assert.assertTrue(context.getAccounts().isEmpty());
            }

            if (RefreshableItem.hasTransactions(sortedItems)) {
                context.processTransactions();
            } else {
                Assert.assertTrue(context.getTransactions().isEmpty());
            }

            if (refreshableItems.contains(RefreshableItem.EINVOICES)) {
                context.processEinvoices();
            } else {
                Assert.assertTrue(context.getTransfers().isEmpty());
            }

            if (refreshableItems.contains(RefreshableItem.TRANSFER_DESTINATIONS)) {
                context.processTransferDestinationPatterns();
            } else {
                Assert.assertTrue(context.getTransferDestinationPatterns().isEmpty());
            }
        } else {
            throw new AssertionError(String.format("%s does not implement a refresh interface.",
                    agent.getClass().getSimpleName()));
        }

        log.info("Done with refresh.");
    }

    public void testRefresh() throws Exception {

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
            // If the credential failed to load (perhaps none previously stored) AND the flags were not overridden
            // == Create new credential
            if (requestFlagCreate == null) {
                requestFlagCreate = true;
            }

            if (requestFlagUpdate == null) {
                requestFlagUpdate = false;
            }
        }

        Agent agent = createAgent(createRefreshInformationRequest());
        try {
            login(agent);
            refresh(agent);
            Assert.assertTrue("Expected to be logged in.", keepAlive(agent));

            if (doLogout) {
                logout(agent);
            }
        } finally {
            saveCredentials(agent);
        }

        context.printCollectedData();
    }

    public static class Builder {
        private static final String DEFAULT_USER_ID = "deadbeefdeadbeefdeadbeefdeadbeef";
        private static final String DEFAULT_CREDENTIAL_ID = "cafebabecafebabecafebabecafebabe";
        private static final String DEFAULT_LOCALE = "sv_SE";

        private final Provider provider;
        private User user = createDefaultUser();
        private Credentials credential = createDefaultCredential();

        private int transactionsToPrint = 32;
        private boolean loadCredentialsBefore = true;
        private boolean saveCredentialsAfter = true;

        // if it should override standard logic
        private Boolean requestFlagCreate = null;
        private Boolean requestFlagUpdate = null;

        private boolean requestFlagManual = true;

        private boolean doLogout = false;

        private Set<RefreshableItem> refreshableItems = new HashSet<>();

        public Builder(String market, String providerName) {
            ProviderConfigModel marketProviders = readProvidersConfiguration(market);
            this.provider = marketProviders.getProvider(providerName);
            this.provider.setMarket(marketProviders.getMarket());
            this.provider.setCurrency(marketProviders.getCurrency());
        }

        private String escapeMarket(String market) {
            return market.replaceAll("[^a-zA-Z]", "");
        }

        private ProviderConfigModel readProvidersConfiguration(String market) {
            String providersFilePath = "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
            File providersFile = new File(providersFilePath);
            try {
                return mapper.readValue(providersFile, ProviderConfigModel.class);
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

        public Boolean getRequestFlagUpdate() {
            return requestFlagUpdate;
        }

        public boolean isRequestFlagManual() {
            return requestFlagManual;
        }

        public boolean isDoLogout() {
            return doLogout;
        }

        public Set<RefreshableItem> getRefreshableItems() {
            return refreshableItems;
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

        public Builder setRequestFlagCreate(boolean requestFlagCreate) {
            this.requestFlagCreate = requestFlagCreate;
            return this;
        }

        public Builder setRequestFlagUpdate(boolean requestFlagUpdate) {
            this.requestFlagUpdate = requestFlagUpdate;
            return this;
        }

        public Builder setRequestFlagManual(boolean requestFlagManual) {
            this.requestFlagManual = requestFlagManual;
            return this;
        }

        public Builder doLogout(boolean doLogout) {
            this.doLogout = doLogout;
            return this;
        }

        public Builder addRefreshableItems(RefreshableItem ...items) {
            this.refreshableItems.addAll(Arrays.asList(items));
            return this;
        }

        public Builder setRefreshableItems(Set<RefreshableItem> refreshableItems) {
            this.refreshableItems = refreshableItems;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setCredentialId(String credentialId) {
            credential.setId(credentialId);
            return this;
        }

        public Builder setCredentialType(CredentialsTypes type) {
            this.credential.setType(type);
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

        public AgentIntegrationTest build() {
            if (refreshableItems.isEmpty()) {
                refreshableItems.addAll(Arrays.asList(RefreshableItem.values()));
            }

            Preconditions.checkNotNull(provider, "Provider was not set.");

            return new AgentIntegrationTest(this);
        }
    }
}
