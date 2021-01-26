package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CollectorSanitizingMigrationTest {

    private static final String PROVIDER_NAME = "collector-bankid";
    private static final String OLD_AGENT_CLASS = "banks.se.collector.CollectorAgent";
    private static final String NEW_AGENT_CLASS = "nxgen.se.banks.collector.CollectorAgent";

    private static final String OLD_ID = "1234567a-123a-123a-123a-1234567890ab";
    private static final String NEW_ID = "1234567a123a123a123a1234567890ab";

    private CollectorSanitizingMigration migration;
    private CredentialsRequest request;
    private Provider provider;

    private List<Account> accountList;
    private Account account;

    @Before
    public void setUp() throws Exception {
        migration = new CollectorSanitizingMigration();
        provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(NEW_AGENT_CLASS);

        accountList = Lists.newArrayList();
        account = new Account();
        account.setBankId(OLD_ID);

        request =
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

        request.setAccounts(accountList);
        request.setProvider(provider);
    }

    @Test
    public void testIsOldAgent() {
        provider.setClassName(OLD_AGENT_CLASS);
        assertTrue(migration.isOldAgent(provider));
        assertFalse(migration.isNewAgent(provider));
    }

    @Test
    public void testIsNewAgent() {
        provider.setClassName(NEW_AGENT_CLASS);
        assertTrue(migration.isNewAgent(provider));
        assertFalse(migration.isOldAgent(provider));
    }

    @Test
    public void testNewAgentClass() {
        provider.setClassName(OLD_AGENT_CLASS);
        assertEquals(NEW_AGENT_CLASS, migration.getNewAgentClassName(provider));
    }

    @Test
    public void testIsDataMigrated() {
        account.setBankId(OLD_ID);
        accountList.add(account);
        assertFalse(migration.isDataMigrated(request));

        account.setBankId(NEW_ID);
        assertTrue(migration.isDataMigrated(request));
    }

    @Test
    public void testMigrateData() {
        account.setBankId(OLD_ID);
        accountList.add(account);
        migration.migrateData(request);
        assertTrue(migration.isDataMigrated(request));

        Account missedAccount = new Account();
        missedAccount.setBankId(OLD_ID);
        accountList.add(missedAccount);
        assertFalse(migration.isDataMigrated(request));

        migration.migrateData(request);
        assertTrue(migration.isDataMigrated(request));
    }
}
