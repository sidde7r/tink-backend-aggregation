package se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAgent;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HandelsbankenBankIdMigrationNoClearingNumberTest {

    public static final String PROVIDER_NAME = "handelsbanken-bankid";
    public static final String NEW_AGENT_NAME = HandelsbankenSEAgent.class.getCanonicalName();
    private static final String OLD_HANDELSBANKEN_AGENT =
            "banks.handelsbanken.v6.HandelsbankenV6Agent";
    private HandelsbankenBankIdMigrationNoClearingNumber migration;
    private CredentialsRequest request;
    private List<Account> accountList;
    private Provider provider;
    private Account oldFormat;
    private Account newFormat;
    private ControllerWrapper wrapper;

    @Before
    public void setUp() throws Exception {
        this.wrapper = Mockito.mock(ControllerWrapper.class);
        this.migration = new HandelsbankenBankIdMigrationNoClearingNumber();
        this.migration.setWrapper(this.wrapper);

        this.provider = new Provider();
        this.provider.setName(PROVIDER_NAME);

        this.accountList = Lists.newArrayList();

        this.oldFormat = new Account();
        this.oldFormat.setBankId("1234-12345678");
        this.oldFormat.setAccountNumber("1234-12 345 678");
        this.oldFormat.setType(AccountTypes.CHECKING);

        this.newFormat = this.oldFormat.clone();
        this.newFormat.setBankId("12345678");

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
        provider.setClassName(OLD_HANDELSBANKEN_AGENT);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }
    @Test
    public void shouldMigrateData_yes() {
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
    public void changeRequest() {
        // For this agent we do not change the request as it uses the same agent
        // so no test
    }

    @Test
    public void migrateData() {
        this.accountList.add(this.oldFormat);

        when(wrapper.updateAccountMetaData(any(String.class), any(String.class)))
                .thenReturn(this.newFormat);

        this.migration.updateAccounts(request);

        verify(wrapper).updateAccountMetaData(this.oldFormat.getId(), this.newFormat.getBankId());

        assertEquals(request.getAccounts().size(), 1);
        assertEquals(
                request.getAccounts().get(0).getAccountNumber(), this.newFormat.getAccountNumber());
        assertEquals(request.getAccounts().get(0).getBankId(), this.newFormat.getBankId());
        assertEquals(request.getAccounts().get(0).getId(), this.newFormat.getId());
    }
}
