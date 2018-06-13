package se.tink.backend.aggregation.agents;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import se.tink.backend.aggregation.AbstractConfigurationBase;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.log.HttpLoggingFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.rpc.UserProfile;
import se.tink.backend.aggregation.utils.CookieContainer;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.common.config.AggregationWorkerConfiguration;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.date.ThreadSafeDateFormat;

public abstract class AbstractAgentTest<T extends Agent> extends AbstractConfigurationBase {
    protected Class<T> cls;
    protected AgentFactory factory;
    private static final AggregationLogger log = new AggregationLogger(AbstractAgentTest.class);
    protected AgentTestContext testContext;

    protected AbstractAgentTest(Class<T> cls) {
        this.cls = cls;

        if (configuration == null) {
            try {
                configuration = CONFIGURATION_FACTORY.build(new File("etc/development.yml"));
            } catch (IOException | ConfigurationException e){
                log.warn("Couldn't set development configuration", e);
            }
        }

        factory = new AgentFactory(configuration);
    }

    protected Provider constructProvider() {
        Provider provider = new Provider();
        provider.setCapabilities(Collections.singleton(Provider.Capability.TRANSFERS));
        return provider;
    }

    protected List<String> constructFeatureFlags() {
        return Lists.newArrayList();
    }

    protected Credentials createCredentials(Map<String, String> fields, CredentialsTypes credentialsType) {
        Credentials c = new Credentials();

        c.setId("----DEMO----");
        c.setType(credentialsType);
        c.setFields(fields);
        c.setUserId(UUID.randomUUID().toString());

        return c;
    }

    protected Credentials createCredentials(String username, String password, CredentialsTypes credentialsType) {
        Credentials c = new Credentials();

        c.setId("76e7e841040242448aad31a4d5e0c2c5");
        c.setUserId("c616a6be3db24471b9d5757188b59beb");
        c.setType(credentialsType);
        c.setUsername(username);
        c.setPassword(password);

        return c;
    }

    protected RefreshInformationRequest createRefreshInformationRequest(Credentials credentials) {
        return createRefreshInformationRequest(credentials, constructProvider());
    }

    protected RefreshInformationRequest createRefreshInformationRequest(Credentials credentials, Provider provider) {
        return new RefreshInformationRequest(createUser(credentials), provider, credentials, true);
    }

    protected User createUser(Credentials credentials) {
        UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");

        User user = new User();
        user.setId(credentials.getUserId());
        user.setProfile(profile);
        user.setFlags(constructFeatureFlags());

        return user;
    }

    protected KeepAliveRequest createKeepAliveRequest(Credentials credentials) {
        UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");

        User user = new User();
        user.setId(credentials.getUserId());
        user.setProfile(profile);
        user.setFlags(constructFeatureFlags());

        return new KeepAliveRequest(user, constructProvider(), credentials);
    }

    protected void testAgent(Credentials credentials) throws Exception {
        testAgent(credentials, true);
    }

    /**
     * Test is done by first creating a agent and then recreate the same agent again and load the stored login session
     */
    protected void testAgentPersistentLoggedIn(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(this, credentials);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        Assert.assertTrue(agent instanceof PersistentLogin);
        PersistentLogin persistentAgent = (PersistentLogin) agent;

        Assert.assertTrue(agent.login());

        // Save Session on the Credentials
        persistentAgent.persistLoginSession();
        // Keep the session alive against the agent (will not really do anything in this test case)
        Assert.assertTrue(persistentAgent.keepAlive());

        // Create a new agent to simulate that we are creating the agent in another request
        persistentAgent = (PersistentLogin) factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        persistentAgent.loadLoginSession();

        Assert.assertTrue(persistentAgent.isLoggedIn());
    }

    protected void keepAliveCommand_willClearSession(Credentials credentials, Class<? extends CookieContainer> sessionCls,
                                                         boolean expected) throws Exception {
        /** Login and save the Session on the Credentials
         *  Then logout to remove the Session from the banks backend
         */
        AgentTestContext testContext = new AgentTestContext(this, credentials);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        Assert.assertTrue(agent instanceof PersistentLogin);
        PersistentLogin persistentAgent = (PersistentLogin) agent;

        Assert.assertTrue(agent.login());

        // Save Session on the Credentials and/or in the agents local memory
        persistentAgent.persistLoginSession();

        // Logout - Should not clear the session
        agent.logout();

        // Create a new agent to simulate that we are creating the agent in another request
        agent = factory.create(cls, createKeepAliveRequest(credentials), testContext);
        persistentAgent = (PersistentLogin) agent;

        // Load the invalid Session
        persistentAgent.loadLoginSession();

        // Credentials has an existing Session
        boolean sessionExists = credentials.getPersistentSession(sessionCls) != null;
        Assert.assertTrue(sessionExists);

        // Forced to fail
        Assert.assertFalse(persistentAgent.keepAlive());
        // Remove Session
        persistentAgent.clearLoginSession();

        // Credentials doesn't have an existing Session unless ( expected = false )
        boolean sessionIsCleared = credentials.getPersistentSession(sessionCls) == null;
        Assert.assertEquals(sessionIsCleared, expected);
    }

    protected void keepAliveCommand_willClearSession(Credentials credentials, Class<? extends CookieContainer> sessionCls)
            throws Exception {
        keepAliveCommand_willClearSession(credentials, sessionCls, true);
    }

    protected void loginCommand_willClearInvalidSession(Credentials credentials, Class<? extends CookieContainer> sessionCls,
                                                        boolean expected) throws Exception {
        /** Login and save the Session on the Credentials
         *  Then logout to remove the Session from the banks backend
         */
        AgentTestContext testContext = new AgentTestContext(this, credentials);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        Assert.assertTrue(agent instanceof PersistentLogin);
        PersistentLogin persistentAgent = (PersistentLogin) agent;

        Assert.assertTrue(agent.login());

        // Save Session on the Credentials and/or in the agents local memory
        persistentAgent.persistLoginSession();

        // Logout - Should not clear the session
        agent.logout();

        // Create a new agent to simulate that we are creating the agent in another request
        agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);
        persistentAgent = (PersistentLogin) agent;

        // Load the invalid Session to the agent
        persistentAgent.loadLoginSession();

        // Credentials has an existing Session
        boolean sessionExists = credentials.getPersistentSession(sessionCls) != null;
        Assert.assertTrue(sessionExists);

        // Forced to fail
        Assert.assertFalse(persistentAgent.isLoggedIn());
        // Remove Session
        persistentAgent.clearLoginSession();

        // Credentials doesn't have an existing Session unless ( expected = false )
        boolean sessionIsCleared = credentials.getPersistentSession(sessionCls) == null;
        Assert.assertEquals(sessionIsCleared, expected);
    }

    protected void loginCommand_willClearInvalidSession(Credentials credentials, Class<? extends CookieContainer> sessionCls)
            throws Exception {
        loginCommand_willClearInvalidSession(credentials, sessionCls, true);
    }

    /**
     * Test to verify that we can handle an expired session. Things that should happen in this scenario - keepalive
     * should be false - isLoggedIn should be false - persisted session should be removed
     */
    protected void testAgentPersistentLoggedInExpiredSession(Credentials credentials, Class<?> sessionType)
            throws Exception {
        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        // Assert that we have some kind of session serialized
        Assert.assertNotNull(credentials.getPersistentSession(sessionType));

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        Assert.assertFalse(persistentAgent.keepAlive());
        Assert.assertFalse(persistentAgent.isLoggedIn());
        persistentAgent.clearLoginSession();

        // Assert that the session is removed
        // Assert that we have some kind of session serialized
        Assert.assertNull(credentials.getPersistentSession(sessionType));
    }

    protected List<Transfer> fetchEInvoices(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(this, credentials);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);
        
        Assert.assertTrue("Agent could not login successfully", agent.login());

        try {
            ((RefreshableItemExecutor) agent).refresh(RefreshableItem.EINVOICES);
            testContext.processEinvoices();
            agent.logout();
        } finally {
            agent.close();
        }

        List<Transfer> transfers = testContext.getTransfers();

        log.debug("Transfers fetched:" + (transfers.isEmpty() ? " <none>" : ""));
        for (Transfer transfer : transfers) {
            log.debug(transfer.toString());
        }

        return transfers;
    }

    protected void testAgent(Credentials credentials, boolean expectsTransactions) throws Exception {
        testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Set the status to UPDATING to mimic the AgentWorker
            credentials.setStatus(CredentialsStatus.UPDATING);

            refresh(agent);

            ImmutableList<CredentialsStatus> validStatuses = ImmutableList
                    .of(CredentialsStatus.UPDATING);
            Assert.assertTrue("Status not valid: " + credentials.getStatus(),
                    validStatuses.contains(credentials.getStatus()));

            Assert.assertTrue(testContext.getAccounts().size() > 0);

            boolean atLeastOneAccountWithMoney = false;
            for (Account account : testContext.getAccounts()) {
                Assert.assertNotNull("account#bankId must not be null or empty: " + account.getBankId(),
                        StringUtils.trimToNull(account.getBankId()));

                // I have seen cases where we parsed garbled data and put it in BankID. This assert should catch cases
                // like
                // that.
                int accountBankIdLength = StringUtils.trim(account.getBankId()).length();
                Assert.assertNotNull(accountBankIdLength < 128);

                if (account.getBalance() != 0) {
                    atLeastOneAccountWithMoney = true;
                }
            }

            Assert.assertTrue("No accounts had any money on them. Expected?", testContext.getAccounts().isEmpty()
                    || atLeastOneAccountWithMoney);

            if (expectsTransactions) {
                Assert.assertTrue(testContext.getTransactions().size() > 0);
            }

            testContext.processTransactions();
            testContext.processTransferDestinationPatterns();

            if (agent instanceof TransferExecutor || agent instanceof TransferExecutorNxgen) {
                testContext.processEinvoices();
            }

            agent.logout();

        } finally {

            agent.close();

        }

    }

    protected AgentTestContext testAgentNoCheck(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(this, credentials);
        testContext.setTestContext(true);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            refresh(agent);

            Assert.assertTrue(credentials.getStatus() == CredentialsStatus.UPDATING);

            agent.logout();

        } finally {

            agent.close();

        }

        return testContext;
    }

    protected void testAgent(Map<String, String> fields) throws Exception {
        testAgent(createCredentials(fields, CredentialsTypes.PASSWORD), true);
    }

    protected void testAgentAuthenticationError(Map<String, String> fields) throws Exception {
        Credentials credentials = createCredentials(fields, CredentialsTypes.PASSWORD);

        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            refresh(agent);

            Assert.assertEquals(CredentialsStatus.AUTHENTICATION_ERROR, credentials.getStatus());

            agent.logout();
        } finally {

            agent.close();

        }

    }

    private void refresh(Agent agent) throws Exception {
        if (agent instanceof RefreshableItemExecutor) {
            RefreshableItemExecutor refreshExecutor = (RefreshableItemExecutor) agent;
            for (RefreshableItem item : RefreshableItem.values()) {
                refreshExecutor.refresh(item);
            }
        } else {
            ((DeprecatedRefreshExecutor) agent).refresh();
        }
    }

    protected void testAgent(String username, String password) throws Exception {
        testAgent(username, password, CredentialsTypes.PASSWORD, true);
    }

    protected void testAgent(String username, String password, String payload) throws Exception {
        Credentials credentials = createCredentials(username, password, CredentialsTypes.PASSWORD);
        credentials.setPayload(payload);
        testAgent(credentials, true);
    }

    protected void testAgent(String username, String password, CredentialsTypes credentialsType) throws Exception {
        testAgent(username, password, credentialsType, true);
    }

    protected void testAgent(String username, String password, CredentialsTypes credentialsType,
            boolean expectsTransactions) throws Exception {
        testAgent(createCredentials(username, password, credentialsType), expectsTransactions);
    }

    protected void testAgentWithSensitivePayload(String username, String password, String payload) throws Exception {
        Credentials credentials = createCredentials(username, password, CredentialsTypes.PASSWORD);
        credentials.setSensitivePayloadSerialized(payload);
        testAgent(credentials);
    }

    protected void testAgentAuthenticationError(String username, String password) throws Exception {
        testAgentAuthenticationError(username, password, CredentialsTypes.PASSWORD);
    }

    protected void testAgentAuthenticationError(String username, String password, CredentialsTypes credentialsType)
            throws Exception {
        testAgentAuthenticationError(createCredentials(username, password, credentialsType));
    }

    protected List<Transfer> fetchEInvoices(String username) throws Exception {
        return fetchEInvoices(username, null);
    }

    protected List<Transfer> fetchEInvoices(String username, String password) throws Exception {
        return fetchEInvoices(createCredentials(username, password, CredentialsTypes.MOBILE_BANKID));
    }

    protected void testAgentAuthenticationError(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertFalse("Agent could not login successfully", agent.login());
        } catch (AuthenticationException e) {
            // This is fine
        }
    }

    protected void testTransfer(String username, String password, CredentialsTypes credentialsType, Transfer transfer)
            throws Exception {
        testTransfer(createCredentials(username, password, credentialsType), transfer);
    }
    
    protected void testUpdateTransfer(String username, String password, CredentialsTypes credentialsType, Transfer transfer)
            throws Exception {
        testUpdateTransfer(createCredentials(username, password, credentialsType), transfer);
    }

    protected void testTransfer(Credentials credentials, Transfer transfer) throws Exception {
        // Create a regular agent.
        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test the transfer.
            SignableOperation signableOperation = new SignableOperation(transfer);


            ClientFilterFactory httpFilter = getHttpLogFilter(credentials, (HttpLoggableExecutor) agent);
            try {
                if (agent instanceof TransferExecutor) {
                    TransferExecutor transferExecutor = (TransferExecutor) agent;
                    transferExecutor.attachHttpFilters(httpFilter);
                    transferExecutor.execute(transfer);
                } else if (agent instanceof TransferExecutorNxgen) {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    transferExecutorNxgen.attachHttpFilters(httpFilter);
                    transferExecutorNxgen.execute(transfer);
                }
            } catch (TransferExecutionException executionException) {
                log.error("Could not execute transfer", executionException);
                signableOperation.setStatus(executionException.getSignableOperationStatus());
                signableOperation.setStatusMessage(executionException.getUserMessage());

                throw new AssertionError("Transfer failed to execute: " + signableOperation.getStatus() +
                        (Strings.isNullOrEmpty(signableOperation.getStatusMessage()) ? "" :
                                " (" + signableOperation.getStatusMessage() + ")"),
                        executionException);
            } finally {
                httpFilter.removeClientFilters();
            }

            agent.logout();
        } finally {
            agent.close();
        }

    }

    protected void testUpdateTransfer(Credentials credentials, Transfer transfer) throws Exception {
        // Create a regular agent.
        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test the transfer.
            SignableOperation signableOperation = new SignableOperation(transfer);
            SignableOperationStatuses outcome = SignableOperationStatuses.EXECUTED;

            ClientFilterFactory httpFilter = getHttpLogFilter(credentials, (HttpLoggableExecutor) agent);
            try {
                if (agent instanceof TransferExecutor) {
                    TransferExecutor transferExecutor = (TransferExecutor) agent;
                    transferExecutor.attachHttpFilters(httpFilter);
                    transferExecutor.update(transfer);
                } else if (agent instanceof TransferExecutorNxgen) {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    transferExecutorNxgen.attachHttpFilters(httpFilter);
                    transferExecutorNxgen.update(transfer);
                }
            } catch (TransferExecutionException e) {
                log.error("Could not execute transfer", e);
                outcome = e.getSignableOperationStatus();
            } finally {
                httpFilter.removeClientFilters();
            }
            signableOperation.setStatus(outcome);

            Assert.assertEquals(
                    "Transfer failed to execute: "
                            + signableOperation.getStatus()
                            + (Strings.isNullOrEmpty(signableOperation.getStatusMessage()) ? "" : " ("
                                    + signableOperation.getStatusMessage()
                                    + ")"), SignableOperationStatuses.EXECUTED, signableOperation.getStatus());

            agent.logout();
        } finally {
            agent.close();
        }

    }

    protected void testTransferException(String username, String password, CredentialsTypes credentialsType,
            Transfer transfer) throws Exception {
        testTransferException(createCredentials(username, password, credentialsType), transfer);
    }

    protected void testTransferException(Credentials credentials, Transfer transfer) throws Exception {

        // Create a regular agent.

        AgentTestContext testContext = new AgentTestContext(this, credentials);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);
        SignableOperation signableOperation = new SignableOperation(transfer);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test transfer and throw whatever exception we get, so that validation can be made outside of this class.

            Assert.assertTrue(
                    "Agent does not implement transfer executor",
                    agent instanceof TransferExecutor || agent instanceof TransferExecutorNxgen);

            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                transferExecutor.execute(transfer);
            } else if (agent instanceof TransferExecutorNxgen) {
                TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                transferExecutorNxgen.execute(transfer);
            }

            agent.logout();
        } finally {
            agent.close();
        }
    }

    protected void testTransferError(String username, String password, CredentialsTypes credentialsType,
            Transfer transfer)
            throws Exception {
        testTransferError(createCredentials(username, password, credentialsType), transfer);
    }

    protected void testTransferError(Credentials credentials, Transfer transfer) throws Exception {
        // Create a regular agent.

        AgentTestContext testContext = new AgentTestContext(this, credentials);

        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        SignableOperation signableOperation = new SignableOperation(transfer);
        try {

            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test the transfer.

            Assert.assertTrue(
                    "Agent does not implement transfer executor",
                    agent instanceof TransferExecutor || agent instanceof TransferExecutorNxgen);

            SignableOperationStatuses outcome = SignableOperationStatuses.EXECUTED;

            ClientFilterFactory httpFilter = getHttpLogFilter(credentials, (HttpLoggableExecutor) agent);
            try {
                if (agent instanceof TransferExecutor) {
                    TransferExecutor transferExecutor = (TransferExecutor) agent;
                    transferExecutor.execute(transfer);
                } else if (agent instanceof TransferExecutorNxgen) {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    transferExecutorNxgen.execute(transfer);
                }
            } catch (TransferExecutionException e) {
                outcome = e.getSignableOperationStatus();
            } finally {
                httpFilter.removeClientFilters();
            }
            Assert.assertNotEquals("Expected to fail. Did not.", SignableOperationStatuses.EXECUTED, outcome);

            agent.logout();
        } finally {

            agent.close();

        }

        Assert.assertTrue(
                "Transfer did not fail: "
                        + signableOperation.getStatus()
                        + (Strings.isNullOrEmpty(signableOperation.getStatusMessage()) ? "" : " ("
                                + signableOperation.getStatusMessage()
                                + ")"), signableOperation.getStatus() == SignableOperationStatuses.FAILED);
    }

    protected void testFetchProductInformation(Credentials credentials, ProductType productType,
            UUID productInstanceId, Map<FetchProductInformationParameterKey, Object> parameters) throws Exception {

        testContext = new AgentTestContext(this, credentials);

        Agent agent = instantiateAgent(createRefreshInformationRequest(credentials));

        Assert.assertTrue("Agent does not implement create product executor", agent instanceof CreateProductExecutor);

        ((CreateProductExecutor)agent).fetchProductInformation(productType, productInstanceId, parameters);
    }

    protected void testRefreshApplication(Credentials credentials, ProductType type, UUID applicationId,
            Map<RefreshApplicationParameterKey, Object> parameters) throws Exception {

        testContext = new AgentTestContext(this, credentials);

        Agent agent = instantiateAgent(createRefreshInformationRequest(credentials));

        Assert.assertTrue("Agent does not implement create product executor", agent instanceof CreateProductExecutor);

        ((CreateProductExecutor)agent).refreshApplication(type, applicationId, parameters);
    }

    private ClientFilterFactory getHttpLogFilter(Credentials credentials, HttpLoggableExecutor transferExecutor) {
        AggregationLogger log = new AggregationLogger(getClass());
        return new HttpLoggingFilterFactory(log, "TRANSFER", ImmutableList.<StringMasker>of(), transferExecutor.getClass());
    }

    public AgentTestContext getTestContext() {
        return testContext;
    }

    protected void testCreateAccount(Credentials credentials, GenericApplication application) throws Exception {
        testCreateAccount(credentials, application, false);
    }

    protected void testCreateAccount(Credentials credentials, GenericApplication application, boolean loginBefore)
            throws Exception {
        Agent agent = instantiateAgent(createRefreshInformationRequest(credentials));

        if (loginBefore) {
            login(agent);
        }

        testCreateAccount(agent, application);

        log.info("Created account");
    }

    private void testCreateAccount(Agent agent, GenericApplication application) throws Exception {
        if (agent instanceof CreateProductExecutor) {
            CreateProductExecutor productExecutor = (CreateProductExecutor) agent;

            try {
                productExecutor.create(application);
            } catch (Exception e) {
                log.warn("Caught exception while creating account", e);
                throw e;
            }
        }
    }

    private Agent instantiateAgent(CredentialsRequest request) throws Exception {
        testContext = new AgentTestContext(this, request.getCredentials());

        Agent agent = factory.create(cls, request, testContext);
        agent.setConfiguration(configuration);

        return agent;
    }

    private void login(Agent agent) throws Exception {
        try {
            if (!agent.login()) {
                throw new AssertionError("Couldn't login due to the login method returned false");
            }
        } catch (Exception e) {
            log.warn("Caught exception on login", e);
            throw e;
        }
    }

    public void writeToDebugFile(Credentials credentials) {
        try {
            File debugDirectory = new File(new AggregationWorkerConfiguration().getDebugLogDir());
            String logContent = testContext.getLogOutputStream().toString("UTF-8");

            if (credentials.getField(Field.Key.PASSWORD) != null) {
                // TODO: Mask all fields that are set as masked in the provider.
                logContent = logContent.replace(credentials.getField(Field.Key.PASSWORD), "******");
            }

            File logFile = new File(debugDirectory, String.format(
                    "%s_%s_u%s_c%s.log",
                    credentials.getProviderName(),
                    ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()),
                    credentials.getUserId(),
                    credentials.getId()));

            Files.write(logContent, logFile, Charsets.UTF_8);

            log.info("Flushed debug log for further investigation: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Could not write logfile for: (provider:"
                    + credentials.getProviderName() + ", credentialsId:" + credentials.getId() + ", status:"
                    + credentials.getStatus() + ", userId:" + credentials.getUserId() + ")", e);
        }
    }
}
