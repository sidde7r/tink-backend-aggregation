package se.tink.backend.aggregation.workers.commands.migrations.implementations.other.csn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public class CSNSanitizingMigrationTest {

    private static final String PROVIDER_NAME = "csn-bankid";
    private static final String OLD_AGENT_CLASS = "legacy.other.csn.CSNAgent";
    private static final String NEW_AGENT_CLASS = "nxgen.se.other.csn.CSNAgent";

    private static final String OLD_ID_ANNUITY_LOAN =
            "199001011234: Lån efter 30 juni 2001 (annuitetslån)";
    private static final String NEW_ID_ANNUITY_LOAN = "9001011234annuitetslan";

    private static final String OLD_ID_STUDENT_LOAN =
            "199001011234: Lån 1 januari 1989-30 juni 2001 (studielån)";
    private static final String NEW_ID_STUDENT_LOAN = "9001011234studielån";

    private static final String OLD_ID_STUDENT_AID = "199001011234: Lån före 1989 (studiemedel)";
    private static final String NEW_ID_STUDENT_AID = "9001011234studiemedel";

    private CSNSanitizingMigration migration;
    private CredentialsRequest request;
    private Provider provider;

    private List<Account> accountList;
    private Account accountAnnuityLoan;
    private Account accountStudentLoan;
    private Account accountStudentAid;

    @Before
    public void setUp() throws Exception {
        migration = new CSNSanitizingMigration();
        provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(NEW_AGENT_CLASS);

        accountList = Lists.newArrayList();
        accountAnnuityLoan = new Account();
        accountStudentLoan = new Account();
        accountStudentAid = new Account();

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
        accountAnnuityLoan.setBankId(OLD_ID_ANNUITY_LOAN);
        accountList.add(accountAnnuityLoan);
        accountStudentLoan.setBankId(OLD_ID_STUDENT_LOAN);
        accountList.add(accountStudentLoan);
        accountStudentAid.setBankId(OLD_ID_STUDENT_AID);
        accountList.add(accountStudentAid);
        assertFalse(migration.isDataMigrated(request));

        accountAnnuityLoan.setBankId(NEW_ID_ANNUITY_LOAN);
        accountStudentLoan.setBankId(NEW_ID_STUDENT_LOAN);
        accountStudentAid.setBankId(NEW_ID_STUDENT_AID);
        assertTrue(migration.isDataMigrated(request));
    }

    @Test
    public void testMigrateData() {
        accountAnnuityLoan.setBankId(OLD_ID_ANNUITY_LOAN);
        accountList.add(accountAnnuityLoan);
        accountStudentLoan.setBankId(OLD_ID_STUDENT_LOAN);
        accountList.add(accountStudentLoan);
        accountStudentAid.setBankId(OLD_ID_STUDENT_AID);
        accountList.add(accountStudentAid);
        migration.migrateData(request);
        assertTrue(migration.isDataMigrated(request));

        Account missedAccountAnnuityLoan = new Account();
        Account missedAccountStudentLoan = new Account();
        Account missedAccountStudentAid = new Account();
        missedAccountAnnuityLoan.setBankId(OLD_ID_ANNUITY_LOAN);
        missedAccountStudentLoan.setBankId(OLD_ID_STUDENT_LOAN);
        missedAccountStudentAid.setBankId(OLD_ID_STUDENT_AID);
        accountList.add(missedAccountAnnuityLoan);
        accountList.add(missedAccountStudentLoan);
        accountList.add(missedAccountStudentAid);
        assertFalse(migration.isDataMigrated(request));

        migration.migrateData(request);
        assertTrue(migration.isDataMigrated(request));
    }
}
