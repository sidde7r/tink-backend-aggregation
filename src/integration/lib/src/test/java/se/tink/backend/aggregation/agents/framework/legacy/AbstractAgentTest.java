package se.tink.backend.aggregation.agents.framework.legacy;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.modules.production.AgentIntegrationTestModule;
import se.tink.backend.aggregation.configuration.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationWrapper;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;
import se.tink.backend.aggregation.nxgen.http.log.HttpLoggingFilterFactory;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.utils.CookieContainer;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public abstract class AbstractAgentTest<T extends Agent> extends AbstractConfigurationBase {
    private static final org.slf4j.Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected Class<T> cls;
    protected AgentFactory factory;
    protected AgentTestContext testContext;

    protected AbstractAgentTest(Class<T> cls) {
        this.cls = cls;

        if (configuration == null) {
            try {
                AgentsServiceConfigurationWrapper agentsServiceConfigurationWrapper =
                        CONFIGURATION_FACTORY.build(new File("etc/development.yml"));
                configuration = agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();
            } catch (IOException | ConfigurationException e) {
                logger.warn("Couldn't set development configuration", e);
            }
        }

        // Provide AgentFactory with 'production' components.
        final Injector injector =
                Guice.createInjector(new AgentIntegrationTestModule(configuration));
        factory = injector.getInstance(AgentFactory.class);
    }

    protected Provider constructProvider() {
        return new Provider();
    }

    protected List<String> constructFeatureFlags() {
        return Lists.newArrayList();
    }

    protected Credentials createCredentials(
            Map<String, String> fields, CredentialsTypes credentialsType) {
        Credentials c = new Credentials();

        c.setId("----DEMO----");
        c.setType(credentialsType);
        c.setFields(fields);
        c.setUserId(UUID.randomUUID().toString());

        return c;
    }

    protected Credentials createCredentials(
            String username, String password, CredentialsTypes credentialsType) {
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

    protected RefreshInformationRequest createRefreshInformationRequest(
            Credentials credentials, Provider provider) {
        return RefreshInformationRequest.builder()
                .user(createUser(credentials))
                .provider(provider)
                .credentials(credentials)
                .originatingUserIp("127.0.0.1")
                .manual(true)
                .forceAuthenticate(false)
                .build();
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
     * Test is done by first creating a agent and then recreate the same agent again and load the
     * stored login session
     */
    protected void testAgentPersistentLoggedIn(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(credentials);
        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        Assert.assertTrue(agent instanceof PersistentLogin);
        PersistentLogin persistentAgent = (PersistentLogin) agent;

        Assert.assertTrue(agent.login());

        // Save Session on the Credentials
        persistentAgent.persistLoginSession();
        // Keep the session alive against the agent (will not really do anything in this test case)
        Assert.assertTrue(persistentAgent.keepAlive());

        // Create a new agent to simulate that we are creating the agent in another request
        persistentAgent =
                (PersistentLogin)
                        factory.create(
                                cls, createRefreshInformationRequest(credentials), testContext);

        persistentAgent.loadLoginSession();

        Assert.assertTrue(persistentAgent.isLoggedIn());
    }

    protected void keepAliveCommand_willClearSession(
            Credentials credentials, Class<? extends CookieContainer> sessionCls, boolean expected)
            throws Exception {
        /**
         * Login and save the Session on the Credentials Then logout to remove the Session from the
         * banks backend
         */
        AgentTestContext testContext = new AgentTestContext(credentials);
        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

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

    /**
     * Test to verify that we can handle an expired session. Things that should happen in this
     * scenario - keepalive should be false - isLoggedIn should be false - persisted session should
     * be removed
     */
    protected void testAgentPersistentLoggedInExpiredSession(
            Credentials credentials, Class<?> sessionType) throws Exception {
        AgentTestContext testContext = new AgentTestContext(credentials);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

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
        AgentTestContext testContext = new AgentTestContext(credentials);
        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        Assert.assertTrue("Agent could not login successfully", agent.login());

        try {
            RefreshExecutorUtils.executeSegregatedRefresher(
                    agent, RefreshableItem.EINVOICES, testContext);
            testContext.processEinvoices();
            agent.logout();
        } finally {
            agent.close();
        }

        List<Transfer> transfers = testContext.getTransfers();

        logger.debug("Transfers fetched: {}", (transfers.isEmpty() ? " <none>" : ""));
        for (Transfer transfer : transfers) {
            logger.debug(transfer.toString());
        }

        return transfers;
    }

    protected void testAgent(Credentials credentials, boolean expectsTransactions)
            throws Exception {
        testContext = new AgentTestContext(credentials);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Set the status to UPDATING to mimic the AgentWorker
            credentials.setStatus(CredentialsStatus.UPDATING);

            refresh(agent);

            ImmutableList<CredentialsStatus> validStatuses =
                    ImmutableList.of(CredentialsStatus.UPDATING);
            Assert.assertTrue(
                    "Status not valid: " + credentials.getStatus(),
                    validStatuses.contains(credentials.getStatus()));

            Assert.assertTrue(testContext.getUpdatedAccounts().size() > 0);

            boolean atLeastOneAccountWithMoney = false;
            for (Account account : testContext.getUpdatedAccounts()) {
                Assert.assertNotNull(
                        "account#bankId must not be null or empty: " + account.getBankId(),
                        StringUtils.trimToNull(account.getBankId()));

                // I have seen cases where we parsed garbled data and put it in BankID. This assert
                // should catch cases
                // like
                // that.
                int accountBankIdLength = StringUtils.trim(account.getBankId()).length();
                Assert.assertNotNull(accountBankIdLength < 128);

                if (account.getBalance() != 0) {
                    atLeastOneAccountWithMoney = true;
                }
            }

            Assert.assertTrue(
                    "No accounts had any money on them. Expected?",
                    testContext.getUpdatedAccounts().isEmpty() || atLeastOneAccountWithMoney);

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
        AgentTestContext testContext = new AgentTestContext(credentials);
        testContext.setTestContext(true);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

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

    private void refresh(Agent agent) throws Exception {
        if (agent instanceof DeprecatedRefreshExecutor) {
            ((DeprecatedRefreshExecutor) agent).refresh();
        } else {
            for (RefreshableItem item :
                    RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL)) {
                RefreshExecutorUtils.executeSegregatedRefresher(agent, item, testContext);
            }
        }
    }

    protected void testAgent(String username, String password) throws Exception {
        testAgent(username, password, CredentialsTypes.PASSWORD, true);
    }

    protected void testAgent(String username, String password, CredentialsTypes credentialsType)
            throws Exception {
        testAgent(username, password, credentialsType, true);
    }

    protected void testAgent(
            String username,
            String password,
            CredentialsTypes credentialsType,
            boolean expectsTransactions)
            throws Exception {
        testAgent(createCredentials(username, password, credentialsType), expectsTransactions);
    }

    protected void testAgentWithSensitivePayload(String username, String password, String payload)
            throws Exception {
        Credentials credentials = createCredentials(username, password, CredentialsTypes.PASSWORD);
        credentials.setSensitivePayloadSerialized(payload);
        testAgent(credentials);
    }

    protected void testAgentAuthenticationError(String username, String password) throws Exception {
        testAgentAuthenticationError(username, password, CredentialsTypes.PASSWORD);
    }

    protected void testAgentAuthenticationError(
            String username, String password, CredentialsTypes credentialsType) throws Exception {
        testAgentAuthenticationError(createCredentials(username, password, credentialsType));
    }

    protected List<Transfer> fetchEInvoices(String username) throws Exception {
        return fetchEInvoices(username, null);
    }

    protected List<Transfer> fetchEInvoices(String username, String password) throws Exception {
        return fetchEInvoices(
                createCredentials(username, password, CredentialsTypes.MOBILE_BANKID));
    }

    protected void testAgentAuthenticationError(Credentials credentials) throws Exception {
        AgentTestContext testContext = new AgentTestContext(credentials);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertFalse("Agent could not login successfully", agent.login());
        } catch (AuthenticationException e) {
            // This is fine
        }
    }

    protected void testTransfer(
            String username, String password, CredentialsTypes credentialsType, Transfer transfer)
            throws Exception {
        testTransfer(createCredentials(username, password, credentialsType), transfer);
    }

    protected void testTransfer(Credentials credentials, Transfer transfer) throws Exception {
        // Create a regular agent.
        AgentTestContext testContext = new AgentTestContext(credentials);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test the transfer.
            SignableOperation signableOperation = new SignableOperation(transfer);

            ClientFilterFactory httpFilter =
                    getHttpLogFilter(credentials, (HttpLoggableExecutor) agent);
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
                logger.error("Could not execute transfer", executionException);
                signableOperation.setStatus(executionException.getSignableOperationStatus());
                signableOperation.setStatusMessage(executionException.getUserMessage());

                throw new AssertionError(
                        "Transfer failed to execute: "
                                + signableOperation.getStatus()
                                + (Strings.isNullOrEmpty(signableOperation.getStatusMessage())
                                        ? ""
                                        : " (" + signableOperation.getStatusMessage() + ")"),
                        executionException);
            } finally {
                httpFilter.removeClientFilters();
            }

            agent.logout();
        } finally {
            agent.close();
        }
    }

    protected void testTransferException(Credentials credentials, Transfer transfer)
            throws Exception {

        // Create a regular agent.

        AgentTestContext testContext = new AgentTestContext(credentials);
        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        try {
            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test transfer and throw whatever exception we get, so that validation can be made
            // outside of this class.

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

    protected void testTransferError(
            String username, String password, CredentialsTypes credentialsType, Transfer transfer)
            throws Exception {
        testTransferError(createCredentials(username, password, credentialsType), transfer);
    }

    protected void testTransferError(Credentials credentials, Transfer transfer) throws Exception {
        // Create a regular agent.

        AgentTestContext testContext = new AgentTestContext(credentials);

        Agent agent =
                factory.create(cls, createRefreshInformationRequest(credentials), testContext);

        SignableOperation signableOperation = new SignableOperation(transfer);
        try {

            Assert.assertTrue("Agent could not login successfully", agent.login());

            // Test the transfer.

            Assert.assertTrue(
                    "Agent does not implement transfer executor",
                    agent instanceof TransferExecutor || agent instanceof TransferExecutorNxgen);

            SignableOperationStatuses outcome = SignableOperationStatuses.EXECUTED;

            ClientFilterFactory httpFilter =
                    getHttpLogFilter(credentials, (HttpLoggableExecutor) agent);
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
            Assert.assertNotEquals(
                    "Expected to fail. Did not.", SignableOperationStatuses.EXECUTED, outcome);

            agent.logout();
        } finally {

            agent.close();
        }

        Assert.assertTrue(
                "Transfer did not fail: "
                        + signableOperation.getStatus()
                        + (Strings.isNullOrEmpty(signableOperation.getStatusMessage())
                                ? ""
                                : " (" + signableOperation.getStatusMessage() + ")"),
                signableOperation.getStatus() == SignableOperationStatuses.FAILED);
    }

    private ClientFilterFactory getHttpLogFilter(
            Credentials credentials, HttpLoggableExecutor transferExecutor) {
        return new HttpLoggingFilterFactory(
                "TRANSFER",
                testContext.getLogMasker(),
                transferExecutor.getClass(),
                LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
    }
}
