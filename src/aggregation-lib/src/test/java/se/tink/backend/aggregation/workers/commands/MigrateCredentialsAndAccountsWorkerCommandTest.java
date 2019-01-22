package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.AgentVersionMigration;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class MigrateCredentialsAndAccountsWorkerCommandTest {

    ControllerWrapper wrapper;
    ImmutableList<AgentVersionMigration> migrations = ImmutableList.of();
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @Before
    public void setUp() {
        wrapper = Mockito.mock(ControllerWrapper.class);
    }

    @Test
    public void testMigrationCommandReturnsContinueWithEmptyMigrationsList() throws Exception {
        MigrateCredentialsAndAccountsWorkerCommand command = createCommand(createRequest());

        AgentWorkerCommandResult result = command.execute();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void testMigrationCommandReturnsContinueWithSomethingInMigrationsListThatWontExecute()
            throws Exception {
        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(
                        createRequest(),
                        Lists.newArrayList(
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
                                    public void migrateData(
                                            ControllerWrapper controllerWrapper,
                                            CredentialsRequest request) {}
                                }));

        AgentWorkerCommandResult result = command.execute();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void testMigrationCommandCallsCorrectMethods_whenShouldMigrate() throws Exception {
        AtomicBoolean shouldChangeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean shouldMigrateDataCalled = new AtomicBoolean(false);
        AtomicBoolean changeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean migrateDataCalled = new AtomicBoolean(false);

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(
                        createRequest(),
                        Lists.newArrayList(
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
                                    public void migrateData(
                                            ControllerWrapper controllerWrapper,
                                            CredentialsRequest request) {
                                        migrateDataCalled.set(true);
                                    }
                                }));

        AgentWorkerCommandResult result = command.execute();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        Assert.assertTrue(shouldChangeRequestCalled.get());
        Assert.assertTrue(shouldMigrateDataCalled.get());
        Assert.assertTrue(changeRequestCalled.get());
        Assert.assertTrue(migrateDataCalled.get());
    }
    @Test
    public void testMigrationCommandCallsCorrectMethods() throws Exception {
        AtomicBoolean shouldChangeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean shouldMigrateDataCalled = new AtomicBoolean(false);
        AtomicBoolean changeRequestCalled = new AtomicBoolean(false);
        AtomicBoolean migrateDataCalled = new AtomicBoolean(false);

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(
                        createRequest(),
                        Lists.newArrayList(
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
                                    public void migrateData(
                                            ControllerWrapper controllerWrapper,
                                            CredentialsRequest request) {
                                        migrateDataCalled.set(true);
                                    }
                                }));

        AgentWorkerCommandResult result = command.execute();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        Assert.assertTrue(shouldChangeRequestCalled.get());
        Assert.assertTrue(shouldMigrateDataCalled.get());
        Assert.assertTrue(changeRequestCalled.get());
        Assert.assertFalse(migrateDataCalled.get());
    }

    @Test
    public void testMigrationCommandChangesSameInstanceOfRequestObject() throws Exception {
        CredentialsRequest request = createRequest();
        Assert.assertEquals("someClassName", request.getProvider().getClassName());
        Account a = request.getAccounts().get(0);

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(
                        request,
                        Lists.newArrayList(
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
                                    public void migrateData(
                                            ControllerWrapper controllerWrapper,
                                            CredentialsRequest request) {}
                                }));

        AgentWorkerCommandResult result = command.execute();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);

        Assert.assertEquals("someClassNameVersion2", request.getProvider().getClassName());
        // also verify account is unchanged
        Assert.assertEquals(
                SerializationUtils.serializeToString(a),
                SerializationUtils.serializeToString(request.getAccounts().get(0)));
        Assert.assertEquals(a, request.getAccounts().get(0));
    }

    @Test
    public void testMigrationCommandChangesSameInstanceOfAccountObject() throws Exception {
        CredentialsRequest request = createRequest();
        Assert.assertFalse(request.isUpdate());
        Account a = request.getAccounts().get(0);
        Assert.assertEquals("b-a-n-k-i-d", request.getAccounts().get(0).getBankId());

        Account clone = a.clone();
        clone.setBankId("bankid");

        Mockito.when(wrapper.updateAccountMetaData(any(String.class), any(String.class)))
                .thenReturn(clone);

        MigrateCredentialsAndAccountsWorkerCommand command =
                createCommand(
                        request,
                        Lists.newArrayList(
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
                                    public void migrateData(
                                            ControllerWrapper controllerWrapper,
                                            CredentialsRequest request) {
                                        try {
                                            Account a = request.getAccounts().get(0).clone();
                                            a.setBankId(a.getBankId().replaceAll("-", ""));
                                            migrateAccounts(
                                                    wrapper, request, Lists.newArrayList(a));
                                        } catch (CloneNotSupportedException e) {
                                            Assert.fail("should allow cloning");
                                        }
                                    }
                                }));
        AgentWorkerCommandResult result = command.execute();
        // this verifies a specific implementation instead of just input/output. But it'll do for
        // now
        verify(wrapper, Mockito.atLeastOnce())
                .updateAccountMetaData(any(String.class), argumentCaptor.capture());
        String newBankId = argumentCaptor.getValue();

        //        Assert.assertEquals(request.getUser().getId(), .getUser());
        //        Assert.assertEquals(request.getCredentials().getId(),
        // newBankId.getCredentialsId());
        //        Assert.assertEquals(request.getAccounts().get(0).getUserId(),
        // newBankId.getAccount().getUserId());
        //        Assert.assertEquals("bankid", newBankId.getAccount().getBankId());

        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
        //Same values, but different instances
        Assert.assertNotEquals(a, request.getAccounts().get(0));
        Assert.assertEquals(newBankId, request.getAccounts().get(0).getBankId());
        Assert.assertTrue(request.isUpdate());
    }

    private CredentialsRequest createRequest() {
        CredentialsRequest request = new RefreshInformationRequest();

        request.setProvider(createProvider());
        request.setUser(createUser());
        request.setCredentials(createCredentials(request.getUser().getId()));
        request.setAccounts(
                Lists.newArrayList(
                        createAccount(
                                request.getUser().getId(), request.getCredentials().getId())));

        return request;
    }

    private Provider createProvider() {
        Provider p = new Provider();
        p.setClassName("someClassName");
        p.setName("someName");
        p.setMarket("SE");
        return p;
    }

    private User createUser() {
        User u = new User();
        u.setId(StringUtils.generateUUID());
        return u;
    }

    private Credentials createCredentials(String userId) {
        Credentials c = new Credentials();
        c.setId(StringUtils.generateUUID());
        c.setUserId(userId);
        return c;
    }

    private Account createAccount(String userId, String credentialsId) {
        Account a = new Account();
        a.setBankId("b-a-n-k-i-d");
        a.setId(StringUtils.generateUUID());
        a.setUserId(userId);
        a.setCredentialsId(credentialsId);
        return a;
    }

    private MigrateCredentialsAndAccountsWorkerCommand createCommand(CredentialsRequest request) {
        return new MigrateCredentialsAndAccountsWorkerCommand(request, wrapper, migrations);
    }

    private MigrateCredentialsAndAccountsWorkerCommand createCommand(
            CredentialsRequest request, List<AgentVersionMigration> migrations) {
        return new MigrateCredentialsAndAccountsWorkerCommand(
                request, wrapper, ImmutableList.copyOf(migrations));
    }
}
