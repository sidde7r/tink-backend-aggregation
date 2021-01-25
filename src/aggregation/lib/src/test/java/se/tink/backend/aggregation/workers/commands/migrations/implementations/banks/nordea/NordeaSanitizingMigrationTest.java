package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaSanitizingMigrationTest {
    private static final String PROVIDER_NAME = "se-nordea-bankid";
    private static final String OLD_AGENT_CLASS = "banks.nordea.NordeaAgent";
    private static final String NEW_AGENT_CLASS = "nxgen.se.banks.nordea.v30.NordeaSEAgent";

    private NordeaSanitizingMigration migration;

    private ArrayList<Account> accountList;
    private CredentialsRequest request;
    private Account oldFormat;
    private Account newFormat;
    private Account oldInvestment;
    private Account newInvestment;

    @Before
    public void setUp() throws Exception {
        migration = new NordeaSanitizingMigration();
        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(OLD_AGENT_CLASS);
        request =
                new CredentialsRequest() {
                    Credentials credentials;

                    @Override
                    public boolean isManual() {
                        return true;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return CredentialsRequestType.UPDATE;
                    }

                    @Override
                    public Credentials getCredentials() {
                        if (credentials == null) {
                            credentials = new Credentials();
                            credentials.setType(CredentialsTypes.MOBILE_BANKID);
                        }

                        return credentials;
                    }
                };

        oldFormat = new Account();
        oldFormat.setBankId("************1234");
        oldFormat.setType(AccountTypes.CHECKING);

        newFormat = oldFormat.clone();
        newFormat.setBankId("1234");

        oldInvestment = new Account();
        oldInvestment.setBankId("ISK:012304540789");
        oldInvestment.setType(AccountTypes.INVESTMENT);

        newInvestment = oldInvestment.clone();
        newInvestment.setBankId("ISK012304540789");

        accountList = Lists.newArrayList();
        request.setAccounts(accountList);
        request.setProvider(provider);
    }

    @Test
    public void isOldAgentTest() {

        Provider oldProvider = new Provider();
        oldProvider.setName(PROVIDER_NAME);
        oldProvider.setClassName(OLD_AGENT_CLASS);

        Provider newProvider = new Provider();
        newProvider.setName(PROVIDER_NAME);
        newProvider.setClassName(NEW_AGENT_CLASS);

        assertTrue(migration.isOldAgent(oldProvider));
        assertFalse(migration.isOldAgent(newProvider));
    }

    @Test
    public void isNewAgentTest() {
        Provider oldProvider = new Provider();
        oldProvider.setName(PROVIDER_NAME);
        oldProvider.setClassName(OLD_AGENT_CLASS);

        Provider newProvider = new Provider();
        newProvider.setName(PROVIDER_NAME);
        newProvider.setClassName(NEW_AGENT_CLASS);

        assertTrue(migration.isNewAgent(newProvider));
        assertFalse(migration.isNewAgent(oldProvider));
    }

    @Test
    public void getNewAgentClassNameTest() {
        Provider oldProvider = new Provider();
        oldProvider.setName(PROVIDER_NAME);
        oldProvider.setClassName(OLD_AGENT_CLASS);
        assertEquals(NEW_AGENT_CLASS, migration.getNewAgentClassName(oldProvider));
    }

    @Test
    public void isDataMigratedTest() {
        accountList = Lists.newArrayList(newFormat);
        request.setAccounts(accountList);
        assertTrue(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(oldFormat);
        request.setAccounts(accountList);
        assertFalse(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(newInvestment);
        request.setAccounts(accountList);
        assertTrue(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(oldInvestment);
        request.setAccounts(accountList);
        assertFalse(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(newInvestment);
        request.setAccounts(accountList);
        request.getCredentials().setType(CredentialsTypes.PASSWORD);
        assertFalse(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(newInvestment);
        request.setAccounts(accountList);
        request.getCredentials().setType(CredentialsTypes.MOBILE_BANKID);
        assertTrue(migration.isDataMigrated(request));
    }

    @Test
    public void migrateData() {
        request =
                new CredentialsRequest() {
                    Credentials credentials;

                    @Override
                    public boolean isManual() {
                        return true;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return CredentialsRequestType.UPDATE;
                    }

                    @Override
                    public Credentials getCredentials() {
                        if (credentials == null) {
                            credentials = new Credentials();
                            credentials.setType(CredentialsTypes.PASSWORD);
                        }

                        return credentials;
                    }
                };

        accountList = Lists.newArrayList(oldFormat, oldInvestment);
        request.setAccounts(accountList);
        migration.migrateData(request);
        assertEquals(2, request.getAccounts().size());
        assertEquals(this.newFormat.getBankId(), request.getAccounts().get(0).getBankId());
        assertEquals(this.newInvestment.getBankId(), request.getAccounts().get(1).getBankId());
        assertEquals(CredentialsTypes.MOBILE_BANKID, request.getCredentials().getType());
    }
}
