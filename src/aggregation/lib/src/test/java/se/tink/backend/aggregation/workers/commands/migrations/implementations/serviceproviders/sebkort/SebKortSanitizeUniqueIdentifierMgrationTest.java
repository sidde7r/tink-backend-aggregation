package se.tink.backend.aggregation.workers.commands.migrations.implementations.serviceproviders.sebkort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
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

public class SebKortSanitizeUniqueIdentifierMgrationTest {

    private static final String PROVIDER_NAME = "saseurobonusmastercard-bankid";
    private static final String NEW_AGENT_NAME =
            "nxgen.se.creditcards.sebkort.saseurobonus.SasEurobonusMastercardSEAgent";
    private static final String OLD_AGENT_NAME = "creditcards.sebkort.SEBKortAgent";

    private static final String SASEB_PAYLOAD = "sase:0102";

    private SebKortSanitizeUniqueIdentifierMgration migration;
    private Provider provider;

    private CredentialsRequest request;
    private List<Account> accountList;

    private Account oldFormat;
    private Account newFormat;

    private ControllerWrapper wrapper;

    @Before
    public void setUp() throws Exception {
        this.wrapper = Mockito.mock(ControllerWrapper.class);
        when(this.wrapper.updateAccountMetaData(any(), any())).thenReturn(null);

        this.migration = new SebKortSanitizeUniqueIdentifierMgration();
        this.migration.setWrapper(this.wrapper);
        this.provider = new Provider();
        this.provider.setName(PROVIDER_NAME);
        this.provider.setClassName(OLD_AGENT_NAME);
        this.provider.setPayload(SASEB_PAYLOAD);

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

    @Test
    public void shouldChangeRequest_no() {
        // For this agent we migrate if the request has the same agent!
        provider.setClassName(NEW_AGENT_NAME + "xx");
        assertFalse(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_sameAgent_yes() {
        provider.setClassName(NEW_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_oldAgent_yes() {
        provider.setClassName(OLD_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldMigrateData_olddata_oldagent() {
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_olddata_newagent() {
        this.request.getProvider().setClassName(NEW_AGENT_NAME);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertTrue(migrateData);
    }

    @Test
    public void shouldMigrateData_alreadyNewFormat_no() {
        this.request.getAccounts().add(this.newFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrate_onlyLoans() {
        this.oldFormat.setType(AccountTypes.LOAN);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrate_onlyInvestments() {
        this.oldFormat.setType(AccountTypes.INVESTMENT);
        this.request.getAccounts().add(this.oldFormat);
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void shouldMigrateData_noAccountsToMigrate_noAccounts() {
        boolean migrateData = migration.shouldMigrateData(request);
        assertFalse(migrateData);
    }

    @Test
    public void changeRequest_oldAgent_olddata() {
        this.accountList.add(this.oldFormat);
        this.migration.changeRequest(request);
        assertEquals(OLD_AGENT_NAME, this.request.getProvider().getClassName());
    }

    @Test
    public void changeRequest_oldAgent_newdata() {
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
