package se.tink.backend.aggregation.workers.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration.MigrationResult;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.MigrationFailedException;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.generic.GenericIdFromAccountNumberMigration;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.generic.GenericSanitizingMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.MigrateCredentialsRequest;

public class MigrateCredentialWorkerCommandTest {

    private DataVersionMigration migration_v1_v2;
    private DataVersionMigration migration_v2_v3;
    private ImmutableMap<String, List<DataVersionMigration>> migrations;
    private AgentWorkerCommandContext context;
    private ControllerWrapper wrapper;

    @Before
    public void setUp() throws Exception {
        migration_v1_v2 =
                new DataVersionMigration() {
                    @Override
                    public int getMigrationFromVersion() {
                        return 1;
                    }

                    @Override
                    protected Map<Account, String> migrateData(
                            CredentialsRequest request, ClientInfo clientInfo) {
                        return new HashMap<>();
                    }
                };

        migration_v2_v3 =
                new DataVersionMigration() {
                    @Override
                    public int getMigrationFromVersion() {
                        return 2;
                    }

                    @Override
                    protected Map<Account, String> migrateData(
                            CredentialsRequest request, ClientInfo clientInfo) {
                        return new HashMap<>();
                    }
                };

        migrations =
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(migration_v1_v2, migration_v2_v3))
                        .build();

        context = Mockito.mock(AgentWorkerCommandContext.class);
        wrapper = Mockito.mock(ControllerWrapper.class);
    }

    private void checkMigrationOrder(String providerName, List<DataVersionMigration> migrations) {
        final int[] sortedDistinctFromVersions =
                migrations.stream()
                        .mapToInt(DataVersionMigration::getMigrationFromVersion)
                        .sorted()
                        .distinct()
                        .toArray();

        if (sortedDistinctFromVersions.length != migrations.size()) {
            throw new IllegalStateException(
                    "Duplicate data version migrations for provider " + providerName);
        }

        if (!Arrays.equals(
                sortedDistinctFromVersions,
                migrations.stream()
                        .mapToInt(DataVersionMigration::getMigrationFromVersion)
                        .toArray())) {
            throw new IllegalStateException(
                    "Unordered data version migrations for provider " + providerName);
        }
    }

    @Test
    public void migrationsShouldBeUniqueAndOrdered() {
        // Ensure migrations for each provider are ordered with no duplicates
        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(null, null, 1, context, wrapper);
        command.getMigrations().entrySet().stream()
                .forEach(entry -> checkMigrationOrder(entry.getKey(), entry.getValue()));
    }

    @Test
    public void shouldNotTouchUnrelatedCredentials() {
        // Ensure no migration is done credentials with no migration command
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("hsbc-password");
        final Provider provider = new Provider();
        provider.setName("hsbc-password");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 1, context, wrapper);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertTrue(command.runMigrationChain());
    }

    @Test
    public void shouldNotMigrateV1toV1() {
        // Ensure no migration_v1_v2 is done for v1 -> v1
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 1, context, wrapper);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertTrue(command.runMigrationChain());
    }

    @Test
    public void shouldInvokeMigrationFromV1toV2() {
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 2, context, wrapper);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertFalse(command.runMigrationChain());
    }

    @Test
    public void shouldMigrateFromV1toV2ButNotV3() {
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 2, context, wrapper);

        command.setMigrations(migrations);

        assertTrue(command.runMigrationChain());
        assertEquals(2, credentials.getDataVersion());
    }

    @Test
    public void shouldMigrateFromV1toV3() {
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 3, context, wrapper);

        command.setMigrations(migrations);

        assertTrue(command.runMigrationChain());
        assertEquals(3, credentials.getDataVersion());
    }

    @Test
    public void doVersionMigrationFromV2toV3() {
        Credentials credentials = new Credentials();
        credentials.setDataVersion(2);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 3, context, wrapper);

        command.setMigrations(migrations);

        assertEquals(MigrationResult.MIGRATED, command.runDataVersionMigration(migration_v2_v3));
        assertEquals(3, credentials.getDataVersion());
    }

    @Test
    public void testIsValidMigratedCredentials() {
        CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
        Account acc1 = new Account();
        Account acc2 = new Account();
        Account acc3 = new Account();
        acc1.setBankId("123");
        acc2.setBankId("123");
        acc3.setBankId("234");

        when(request.getAccounts()).thenReturn(Lists.newArrayList(acc1, acc2));
        assertFalse(MigrateCredentialWorkerCommand.isValidMigratedCredentials(request));

        when(request.getAccounts()).thenReturn(Lists.newArrayList(acc1, acc2, acc3));
        assertFalse(MigrateCredentialWorkerCommand.isValidMigratedCredentials(request));

        when(request.getAccounts()).thenReturn(Lists.newArrayList(acc1, acc3));
        assertTrue(MigrateCredentialWorkerCommand.isValidMigratedCredentials(request));
    }

    @Test
    public void shouldGiveAbortResultOnCollision() {
        Account acc1 = new Account();
        Account acc2 = new Account();
        Account acc3 = new Account();
        acc1.setBankId("123");
        acc2.setBankId("123");
        acc3.setBankId("234");
        Credentials credentials = new Credentials();
        credentials.setDataVersion(2);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);
        request.setAccounts(Lists.newArrayList(acc1, acc2));

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 3, context, wrapper);

        command.setMigrations(migrations);

        assertEquals(MigrationResult.ABORT, command.runDataVersionMigration(migration_v2_v3));
        assertEquals(2, credentials.getDataVersion());
    }

    @Test
    public void e2e() throws Exception {
        Account acc = new Account();
        acc.setAccountNumber("123-456");
        acc.setBankId("2be65c23");
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("demo");
        final Provider provider = new Provider();
        provider.setName("demo");

        ImmutableMap<String, List<DataVersionMigration>> migrations =
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put(
                                "demo",
                                Lists.newArrayList(
                                        new GenericIdFromAccountNumberMigration(1),
                                        new GenericSanitizingMigration(2)))
                        .build();

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);
        request.setAccounts(Lists.newArrayList(acc));

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 3, context, wrapper);
        command.setMigrations(migrations);

        final AgentWorkerCommandResult execute = command.execute();
        assertEquals(AgentWorkerCommandResult.CONTINUE, execute);
        assertEquals(3, credentials.getDataVersion());

        command.postProcess();
        verify(context, times(1))
                .updateCredentialsExcludingSensitiveInformation(credentials, true, true);
        verify(wrapper, times(1)).updateAccountMetaData(acc.getId(), "123456");
    }

    private static DataVersionMigration throwingMigration(int from) {
        return new DataVersionMigration() {
            @Override
            public int getMigrationFromVersion() {
                return from;
            }

            @Override
            protected Map<Account, String> migrateData(
                    CredentialsRequest request, ClientInfo clientInfo)
                    throws MigrationFailedException {
                throw new MigrationFailedException();
            }
        };
    }
}
