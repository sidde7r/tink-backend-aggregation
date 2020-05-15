package se.tink.backend.aggregation.workers.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class MigrateCredentialsAndAccountsWorkerCommandTest {

    private static String PROVIDER_NAME = "some-provider";
    private ControllerWrapper wrapper;
    private ImmutableMap<String, AgentVersionMigration> migrations = ImmutableMap.of();
    private ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @Before
    public void setUp() {
        wrapper = mock(ControllerWrapper.class);
    }

    @Test
    public void testMigrationCommandReturnsContinueWithEmptyMigrationsList() throws Exception {
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(createRequest());

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void testMigrationCommandReturnsContinueWhenDifferentProviderName() throws Exception {
        AtomicBoolean shouldChangeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean shouldMigrateDataCalled = new AtomicBoolean(false);
        AtomicBoolean changeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean migrateDataCalled = new AtomicBoolean(false);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME + "x",
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        shouldChangeRequestCalled.set(true);
                        return false;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        shouldMigrateDataCalled.set(true);
                        return false;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {
                        changeRequestCalled.set(true);
                    }

                    @Override
                    public void migrateData(CredentialsRequest request) {
                        migrateDataCalled.set(true);
                    }
                });

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(createRequest(), migrations);

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
        assertFalse(shouldChangeRequestCalled.get());
        assertFalse(shouldMigrateDataCalled.get());
        assertFalse(changeRequestCalled.get());
        assertFalse(migrateDataCalled.get());
    }

    @Test
    public void testMigrationCommandReturnsContinueWithSomethingInMigrationsListThatWontExecute()
            throws Exception {
        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return false;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return false;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {}

                    @Override
                    public void migrateData(CredentialsRequest request) {}
                });
        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(createRequest(), migrations);

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void testMigrationCommandCallsCorrectMethodsWhenShouldMigrate() throws Exception {
        AtomicBoolean shouldChangeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean shouldMigrateDataCalled = new AtomicBoolean(false);
        AtomicBoolean changeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean migrateDataCalled = new AtomicBoolean(false);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        shouldChangeRequestCalled.set(true);
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        shouldMigrateDataCalled.set(true);
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {
                        changeRequestCalled.set(true);
                    }

                    @Override
                    public void migrateData(CredentialsRequest request) {
                        migrateDataCalled.set(true);
                    }
                });

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(createRequest(), migrations);

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        assertTrue(shouldChangeRequestCalled.get());
        assertTrue(shouldMigrateDataCalled.get());
        assertTrue(changeRequestCalled.get());
        assertTrue(migrateDataCalled.get());
    }

    @Test
    public void testMigrationCommandCallsCorrectMethods() throws Exception {
        AtomicBoolean shouldChangeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean shouldMigrateDataCalled = new AtomicBoolean(false);
        AtomicBoolean changeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean migrateDataCalled = new AtomicBoolean(false);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {

                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        shouldChangeRequestCalled.set(true);
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        shouldMigrateDataCalled.set(true);
                        return false;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {
                        changeRequestCalled.set(true);
                    }

                    @Override
                    public void migrateData(CredentialsRequest request) {
                        migrateDataCalled.set(true);
                    }
                });
        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(createRequest(), migrations);

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        assertTrue(shouldChangeRequestCalled.get());
        assertTrue(shouldMigrateDataCalled.get());
        assertTrue(changeRequestCalled.get());
        assertFalse(migrateDataCalled.get());
    }

    @Test
    public void testMigrationCommandChangesSameInstanceOfRequestObject() throws Exception {
        CredentialsRequest request = createRequest();
        assertEquals("someClassName", request.getProvider().getClassName());
        Account a = request.getAccounts().get(0);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return false;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {
                        request.getProvider().setClassName("someClassNameVersion2");
                    }

                    @Override
                    public void migrateData(CredentialsRequest request) {}
                });
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        assertEquals("someClassNameVersion2", request.getProvider().getClassName());
        // also verify account is unchanged
        assertEquals(
                SerializationUtils.serializeToString(a),
                SerializationUtils.serializeToString(request.getAccounts().get(0)));
        assertEquals(a, request.getAccounts().get(0));
    }

    @Test
    public void testMigrationCommandChangesSameInstanceOfAccountObject() throws Exception {
        CredentialsRequest request = createRequest();
        assertFalse(request.isUpdate());
        Account a = request.getAccounts().get(0);
        assertEquals("b-a-n-k-i-d", request.getAccounts().get(0).getBankId());

        Account clone = a.clone();
        clone.setBankId("bankid");

        when(wrapper.updateAccountMetaData(any(String.class), any(String.class))).thenReturn(clone);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {
                        request.setUpdate(true);
                    }

                    @Override
                    public void migrateData(CredentialsRequest request) {
                        try {
                            Account a = request.getAccounts().get(0).clone();
                            a.setBankId(a.getBankId().replaceAll("-", ""));
                            migrateAccounts(request, Lists.newArrayList(a));
                        } catch (CloneNotSupportedException e) {
                            fail("should allow cloning");
                        }
                    }
                });
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);
        AgentWorkerCommandResult result = command.execute();
        // this verifies a specific implementation instead of just input/output. But it'll do for
        // now
        verify(wrapper, atLeastOnce())
                .updateAccountMetaData(any(String.class), argumentCaptor.capture());
        String newBankId = argumentCaptor.getValue();

        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
        // Same values, but different instances
        assertNotEquals(a, request.getAccounts().get(0));
        assertEquals(newBankId, request.getAccounts().get(0).getBankId());
        assertTrue(request.isUpdate());
    }

    @Test
    public void deduplicateAccountsWhenDuplicatesNoneClosed() throws Exception {
        CredentialsRequest request = createRequest();
        Account clone = request.getAccounts().get(0).clone();
        request.getAccounts().add(clone);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {}

                    @Override
                    public void migrateData(CredentialsRequest request) {}

                    @Override
                    protected Account migrateAccount(Account account) {
                        return account;
                    }
                });
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);
        AgentWorkerCommandResult result = command.execute();
        assertEquals(2, request.getAccounts().size());
        List<Account> oldAccount =
                request.getAccounts().stream()
                        .filter(a -> !a.getBankId().contains("-duplicate"))
                        .collect(Collectors.toList());
        assertEquals(1, oldAccount.size());
        assertEquals(
                1,
                request.getAccounts().stream()
                        .filter(a -> a.getBankId().contains("-duplicate"))
                        .count());
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void deduplicateAccountsWhenDuplicatesOneClosed() throws Exception {
        CredentialsRequest request = createRequest();
        Account clone = request.getAccounts().get(0).clone();
        clone.setClosed(true);
        request.getAccounts().add(clone);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {}

                    @Override
                    public void migrateData(CredentialsRequest request) {}

                    @Override
                    protected Account migrateAccount(Account account) {
                        return account;
                    }
                });
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);
        AgentWorkerCommandResult result = command.execute();
        assertEquals(2, request.getAccounts().size());
        List<Account> oldAccount =
                request.getAccounts().stream()
                        .filter(a -> !a.getBankId().contains("-duplicate"))
                        .collect(Collectors.toList());
        assertTrue(oldAccount.get(0).isClosed());
        assertEquals(1, oldAccount.size());
        assertEquals(
                1,
                request.getAccounts().stream()
                        .filter(a -> a.getBankId().contains("-duplicate"))
                        .count());
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void deduplicateAccountsWhenThreeDuplicates() throws Exception {
        CredentialsRequest request = createRequest();
        Account clone1 = request.getAccounts().get(0).clone();
        Account clone2 = request.getAccounts().get(0).clone();

        request.getAccounts().add(clone1);
        request.getAccounts().add(clone2);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {}

                    @Override
                    public void migrateData(CredentialsRequest request) {}

                    @Override
                    protected Account migrateAccount(Account account) {
                        return account;
                    }
                });

        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);

        AgentWorkerCommandResult result = command.execute();

        assertEquals(3, request.getAccounts().size());
        assertEquals(
                1,
                request.getAccounts().stream()
                        .filter(a -> !a.getBankId().contains("-duplicate"))
                        .count());
        assertEquals(
                1,
                request.getAccounts().stream()
                        .filter(a -> a.getBankId().contains("-duplicate-1"))
                        .count());
        assertEquals(
                1,
                request.getAccounts().stream()
                        .filter(a -> a.getBankId().contains("-duplicate-2"))
                        .count());

        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void deduplicateAccountsWhenNoDuplicates() throws Exception {
        CredentialsRequest request = createRequest();
        Account e = request.getAccounts().get(0).clone();
        e.setBankId(e.getBankId() + "x");
        request.getAccounts().add(e);

        Map<String, AgentVersionMigration> migrations = new HashMap<>();
        migrations.put(
                PROVIDER_NAME,
                new AgentVersionMigration() {
                    @Override
                    public boolean shouldChangeRequest(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public boolean shouldMigrateData(CredentialsRequest request) {
                        return true;
                    }

                    @Override
                    public void changeRequest(CredentialsRequest request) {}

                    @Override
                    public void migrateData(CredentialsRequest request) {}

                    @Override
                    protected Account migrateAccount(Account account) {
                        return account;
                    }
                });
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(request, migrations);
        assertEquals(2, request.getAccounts().size());
        assertEquals(
                0,
                request.getAccounts().stream()
                        .filter(a -> a.getBankId().contains("-duplicate"))
                        .count());
        assertEquals(
                2,
                request.getAccounts().stream()
                        .filter(a -> !a.getBankId().contains("-duplicate"))
                        .count());

        AgentWorkerCommandResult result = command.execute();
        assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    private CredentialsRequest createRequest() {
        CredentialsRequest request = new RefreshInformationRequest();

        request.setProvider(createProvider());
        request.setAccounts(Lists.newArrayList(createAccount()));

        return request;
    }

    private Provider createProvider() {
        Provider p = new Provider();
        p.setClassName("someClassName");
        p.setName(PROVIDER_NAME);
        p.setMarket("SE");
        return p;
    }

    private Account createAccount() {
        Account a = new Account();
        a.setBankId("b-a-n-k-i-d");
        a.setId(UUIDUtils.generateUUID());
        return a;
    }

    private MigrateCredentialsAndAccountsWorkerCommand createCommand(CredentialsRequest request) {
        MigrateCredentialsAndAccountsWorkerCommand migrateCredentialsAndAccountsWorkerCommand =
                new MigrateCredentialsAndAccountsWorkerCommand(
                        request, wrapper, createClientInfo());
        migrateCredentialsAndAccountsWorkerCommand.setMigrations(ImmutableMap.copyOf(migrations));
        return migrateCredentialsAndAccountsWorkerCommand;
    }

    private MigrateCredentialsAndAccountsWorkerCommand createCommand(
            CredentialsRequest request, Map<String, AgentVersionMigration> migrations) {
        MigrateCredentialsAndAccountsWorkerCommand migrateCredentialsAndAccountsWorkerCommand =
                new MigrateCredentialsAndAccountsWorkerCommand(
                        request, wrapper, createClientInfo());
        migrateCredentialsAndAccountsWorkerCommand.setMigrations(ImmutableMap.copyOf(migrations));
        return migrateCredentialsAndAccountsWorkerCommand;
    }

    private ClientInfo createClientInfo() {
        return ClientInfo.of("client", "oxford-staging", "aggregator", null);
    }
}
