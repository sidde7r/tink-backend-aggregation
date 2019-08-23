package se.tink.backend.aggregation.workers.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration.MigrationResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.MigrateCredentialsRequest;

public class MigrateCredentialWorkerCommandTest {

    private DataVersionMigration migration_v1_v2;
    private DataVersionMigration migration_v2_v3;
    private ImmutableMap<String, List<DataVersionMigration>> migrations;

    @Before
    public void setUp() throws Exception {
        migration_v1_v2 =
                new DataVersionMigration() {
                    @Override
                    public int getMigrationFromVersion() {
                        return 1;
                    }

                    @Override
                    protected MigrationResult migrateData(CredentialsRequest request) {
                        return MigrationResult.MIGRATED;
                    }
                };

        migration_v2_v3 =
                new DataVersionMigration() {
                    @Override
                    public int getMigrationFromVersion() {
                        return 2;
                    }

                    @Override
                    protected MigrationResult migrateData(CredentialsRequest request) {
                        return MigrationResult.MIGRATED;
                    }
                };

        migrations =
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(migration_v1_v2, migration_v2_v3))
                        .build();
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
                new MigrateCredentialWorkerCommand(request, null, 1);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertTrue(command.successfulMigration());
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
                new MigrateCredentialWorkerCommand(request, null, 1);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertTrue(command.successfulMigration());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldInvokeMigrationFromV1toV2() {
        Credentials credentials = new Credentials();
        credentials.setDataVersion(1);
        credentials.setProviderName("avanza-bankid");
        final Provider provider = new Provider();
        provider.setName("avanza-bankid");

        CredentialsRequest request = new MigrateCredentialsRequest(null, provider, credentials);

        MigrateCredentialWorkerCommand command =
                new MigrateCredentialWorkerCommand(request, null, 2);

        command.setMigrations(
                new ImmutableMap.Builder<String, List<DataVersionMigration>>()
                        .put("avanza-bankid", Lists.newArrayList(throwingMigration(1)))
                        .build());

        assertTrue(command.successfulMigration());
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
                new MigrateCredentialWorkerCommand(request, null, 2);

        command.setMigrations(migrations);

        assertTrue(command.successfulMigration());
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
                new MigrateCredentialWorkerCommand(request, null, 3);

        command.setMigrations(migrations);

        assertTrue(command.successfulMigration());
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
                new MigrateCredentialWorkerCommand(request, null, 3);

        command.setMigrations(migrations);

        assertEquals(MigrationResult.MIGRATED, command.doVersionMigrations(migration_v2_v3));
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
                new MigrateCredentialWorkerCommand(request, null, 3);

        command.setMigrations(migrations);

        assertEquals(MigrationResult.ABORT, command.doVersionMigrations(migration_v2_v3));
        assertEquals(2, credentials.getDataVersion());
    }

    private static DataVersionMigration throwingMigration(int from) {
        return new DataVersionMigration() {
            @Override
            public int getMigrationFromVersion() {
                return from;
            }

            @Override
            protected MigrationResult migrateData(CredentialsRequest request) {
                throw new IllegalStateException();
            }
        };
    }
}
