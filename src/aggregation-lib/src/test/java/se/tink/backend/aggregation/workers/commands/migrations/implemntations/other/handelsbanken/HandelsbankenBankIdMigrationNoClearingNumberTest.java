package se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAgent;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Provider;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HandelsbankenBankIdMigrationNoClearingNumberTest {

    public static final String PRVIDER_NAME = "handelsbanken-bankid";
    public static final String NEW_AGENT_NAME = HandelsbankenSEAgent.class.getCanonicalName();
    public static final String OLD_AGENT_NAME = "HandelsbankenV6";
    private HandelsbankenBankIdMigrationNoClearingNumber migration;
    private CredentialsRequest request;
    private List<Account> accountList;
    private Provider provider;
    private Account oldFormat;
    private Account newFormat;

    @Before
    public void setUp() throws Exception {
        this.migration = new HandelsbankenBankIdMigrationNoClearingNumber();
        this.request = Mockito.mock(CredentialsRequest.class);
        this.provider = Mockito.mock(Provider.class);
        this.accountList = Lists.newArrayList();

        this.oldFormat = new Account();
        this.oldFormat.setBankId("1234-12345678");
        this.oldFormat.setAccountNumber("1234-12 345 678");
        this.oldFormat.setType(AccountTypes.CHECKING);

        this.newFormat = this.oldFormat.clone();
        this.newFormat.setBankId("12345678");

        Mockito.when(request.getProvider()).thenReturn(provider);
        Mockito.when(request.getAccounts()).thenReturn(this.accountList);
    }

    @Test
    public void shouldChangeRequest_yes() {
        Mockito.when(provider.getName()).thenReturn(PRVIDER_NAME);
        Mockito.when(provider.getClassName()).thenReturn(OLD_AGENT_NAME);
        assertTrue(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_differentProvider_no() {
        Mockito.when(provider.getName()).thenReturn(PRVIDER_NAME + 'x');
        assertFalse(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldChangeRequest_sameAgent_no() {
        Mockito.when(provider.getClassName()).thenReturn(NEW_AGENT_NAME);
        assertFalse(this.migration.shouldChangeRequest(this.request));
    }

    @Test
    public void shouldMigrateData_yes() {}

    @Test
    public void shouldMigrateData_alreadyNewFormat_no() {}

    @Test
    public void shouldMigrateData_noAccountsToMgrate_no() {}

    @Test
    public void changeRequest() {
        migration.changeRequest(request);
    }

    @Test
    public void migrateData() {
        Mockito.when(provider.getName()).thenReturn(PRVIDER_NAME);
        Mockito.when(provider.getClassName()).thenReturn(OLD_AGENT_NAME);
        this.accountList.add(this.oldFormat);
        this.migration.migrateData(Mockito.mock(ControllerWrapper.class), request);


    }
}
