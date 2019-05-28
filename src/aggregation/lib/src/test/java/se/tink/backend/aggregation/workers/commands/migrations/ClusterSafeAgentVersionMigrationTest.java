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
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public class ClusterSafeAgentVersionMigrationTest {

    private static final String PROVIDER_NAME = "saseurobonusmastercard-bankid";
    private static final String NEW_AGENT_NAME =
            "nxgen.se.creditcards.sebkort.saseurobonus.SasEurobonusMastercardSEAgent";
    private static final String OLD_AGENT_NAME = "creditcards.sebkort.SEBKortAgent";

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
        this.oldFormat.setBankId("123456******1234");
        this.oldFormat.setType(AccountTypes.CREDIT_CARD);

        this.newFormat = this.oldFormat.clone();
        this.newFormat.setBankId("1234561234");

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

    @Test
    public void shouldChangeRequest_otherAgent() {
        // Run only for our own agents
        provider.setClassName(NEW_AGENT_NAME + "xx");
        assertFalse(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_newAgent() {
        // Run only for our own agents
        provider.setClassName(NEW_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_oldAgent() {
        // Run only for our own agents
        provider.setClassName(OLD_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldMigrateData_oldData_oldAgent() {
        // A request for the old agent with non-migrated credentials will only occur
        // in instances with old config, so we should not migrate the data
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_oldDataNewAgent() {
        // A request for the new agent with non-migrated credentials will only occur
        // in instances with the new config, so we should migrate the data
        this.request.getProvider().setClassName(NEW_AGENT_NAME);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertTrue(migrateData);
    }

    @Test
    public void shouldMigrateData_alreadyNewFormat() {
        // Don't migrate already migrated credentials
        this.request.getAccounts().add(this.newFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrateFiltering() {
        // Don't migrate credentials which the isDataMigrated check filters
        this.oldFormat.setType(AccountTypes.LOAN);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrateFiltering2() {
        // Don't migrate credentials which the isDataMigrated check filters
        this.oldFormat.setType(AccountTypes.INVESTMENT);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrate_noAccounts() {
        // Don't migrate requests without accounts
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void changeRequest_oldAgentOldData() {
        // A request for the old agent with non-migrated credentials will only occur
        // in instances with old config, so we should continue to use the old agent
        this.accountList.add(this.oldFormat);
        this.migration.changeRequest(request);
        assertEquals(OLD_AGENT_NAME, this.request.getProvider().getClassName());
    }

    @Test
    public void changeRequest_oldAgentNewData() {
        // A request for the new agent with non-migrated credentials will only occur
        // in instances with new config, so we should force a switch to the new agent
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
