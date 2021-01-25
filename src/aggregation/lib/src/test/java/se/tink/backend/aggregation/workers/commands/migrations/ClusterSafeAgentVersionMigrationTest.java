package se.tink.backend.aggregation.workers.commands.migrations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ClusterSafeAgentVersionMigrationTest {

    private static final String PROVIDER_NAME = "saseurobonusmastercard-bankid";
    private static final String NEW_AGENT_NAME =
            "nxgen.se.creditcards.sebkort.saseurobonus.SasEurobonusMastercardSEAgent";
    private static final String OLD_AGENT_NAME = "creditcards.sebkort.SEBKortAgent";
    private static final String OLD_FORMAT = "123456******1234";
    private static final String NEW_FORMAT = "1234561234";

    private ClusterSafeAgentVersionMigration migration;
    private Provider provider;

    private CredentialsRequest request;
    private List<Account> accountList;

    private Account oldFormat;
    private Account newFormat;

    @Before
    public void setUp() throws Exception {
        ControllerWrapper wrapper = Mockito.mock(ControllerWrapper.class);
        when(wrapper.updateAccountMetaData(any(), any())).thenReturn(null);

        this.migration = new TestMigration();
        this.migration.setWrapper(wrapper);
        this.provider = new Provider();
        this.provider.setName(PROVIDER_NAME);
        this.provider.setClassName(OLD_AGENT_NAME);

        this.request =
                new CredentialsRequest() {
                    @Override
                    public boolean isManual() {
                        return true;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return CredentialsRequestType.UPDATE;
                    }
                };

        this.oldFormat = new Account();
        this.oldFormat.setBankId(OLD_FORMAT);
        this.oldFormat.setType(AccountTypes.CREDIT_CARD);

        this.newFormat = this.oldFormat.clone();
        this.newFormat.setBankId(NEW_FORMAT);

        this.accountList = Lists.newArrayList();
        this.request.setAccounts(accountList);
        this.request.setProvider(provider);
    }

    private class TestMigration extends ClusterSafeAgentVersionMigration {

        private final Pattern SANITIZED_PATTERN = Pattern.compile("^[0-9]{10}$");

        @Override
        public boolean isOldAgent(Provider provider) {
            return provider.getClassName().equals(OLD_AGENT_NAME);
        }

        @Override
        public boolean isNewAgent(Provider provider) {
            return provider.getClassName().equals(NEW_AGENT_NAME);
        }

        @Override
        public String getNewAgentClassName(Provider oldProvider) {
            return NEW_AGENT_NAME;
        }

        @Override
        public boolean isDataMigrated(CredentialsRequest request) {
            return request.getAccounts().stream()
                    .filter(a -> AccountTypes.CREDIT_CARD == a.getType())
                    .anyMatch(a -> SANITIZED_PATTERN.matcher(a.getBankId()).matches());
        }

        @Override
        public void migrateData(CredentialsRequest request) {
            request.getAccounts().stream()
                    .filter(a -> !SANITIZED_PATTERN.matcher(a.getBankId()).matches())
                    .forEach(a -> a.setBankId(a.getBankId().replaceAll("[^\\dA-Za-z]", "")));
        }
    }

    /** Run only for our own agents */
    @Test
    public void shouldChangeRequest_otherAgent() {
        provider.setClassName(NEW_AGENT_NAME + "xx");
        assertFalse(this.migration.shouldChangeRequest(this.request));
    }

    /** Run only for our own agents */
    @Test
    public void shouldChangeRequest_newAgent() {
        provider.setClassName(NEW_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    /** Run only for our own agents */
    @Test
    public void shouldChangeRequest_oldAgent() {
        provider.setClassName(OLD_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    /**
     * A request for the old agent with non-migrated credentials will only occur in instances with
     * old config, so we should not migrate the data
     */
    @Test
    public void shouldMigrateData_oldDataOldAgent() {
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    /**
     * A request for the new agent with non-migrated credentials will only occur in instances with
     * the new config, so we should migrate the data
     */
    @Test
    public void shouldMigrateData_oldDataNewAgent() {
        this.request.getProvider().setClassName(NEW_AGENT_NAME);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertTrue(migrateData);
    }

    /** Don't migrate already migrated credentials */
    @Test
    public void shouldMigrateData_alreadyNewFormat() {
        this.request.getAccounts().add(this.newFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    /** Don't migrate credentials which the isDataMigrated check filters */
    @Test
    public void shouldMigrateData_noAccountsToMigrateFilteringLoans() {
        this.oldFormat.setType(AccountTypes.LOAN);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    /** Don't migrate credentials which the isDataMigrated check filters */
    @Test
    public void shouldMigrateData_noAccountsToMigrateFilteringInvestments() {
        this.oldFormat.setType(AccountTypes.INVESTMENT);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    /** Don't migrate requests without accounts */
    @Test
    public void shouldMigrateData_noAccountsToMigrate_noAccounts() {
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    /**
     * A request for the old agent with non-migrated credentials will only occur in instances with
     * old config, so we should continue to use the old agent
     */
    @Test
    public void changeRequest_oldAgentOldData() {
        this.accountList.add(this.oldFormat);
        this.migration.changeRequest(request);
        assertEquals(OLD_AGENT_NAME, this.request.getProvider().getClassName());
    }

    /**
     * A request for the new agent with non-migrated credentials will only occur in instances with
     * new config, so we should force a switch to the new agent
     */
    @Test
    public void changeRequest_oldAgentNewData() {
        this.accountList.add(this.newFormat);
        this.migration.changeRequest(request);
        assertEquals(NEW_AGENT_NAME, this.request.getProvider().getClassName());
    }

    @Test
    public void migrateData() {
        this.accountList.add(this.oldFormat);
        this.migration.migrateData(request);

        assertEquals(1, request.getAccounts().size());
        assertEquals(this.newFormat.getBankId(), request.getAccounts().get(0).getBankId());
    }

    @Test
    public void shouldMigrateData_returnsFalseAfterMigration() {
        this.accountList.add(this.oldFormat);
        this.migration.migrateData(request);

        boolean shouldMigrate = migration.shouldMigrateData(request);
        assertFalse(shouldMigrate);
    }
}
