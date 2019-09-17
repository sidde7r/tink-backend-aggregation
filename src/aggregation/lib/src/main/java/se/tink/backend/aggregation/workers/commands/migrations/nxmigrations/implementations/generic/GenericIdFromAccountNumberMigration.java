package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.generic;

import com.google.common.collect.Lists;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class GenericIdFromAccountNumberMigration extends DataVersionMigration {

    private final int fromVersion;

    public GenericIdFromAccountNumberMigration(int fromVersion) {
        this.fromVersion = fromVersion;
    }

    @Override
    public int getMigrationFromVersion() {
        return fromVersion;
    }

    /**
     * Only perform migration if at least one account does *not* have its (sanitized) account number
     * as unique identifier.
     */
    @Override
    protected boolean isAlreadyMigrated(CredentialsRequest request) {
        return Optional.ofNullable(request.getAccounts()).orElseGet(Lists::newArrayList).stream()
                .allMatch(account -> account.getBankId().equals(account.getAccountNumber()));
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "");
    }

    @Override
    protected Map<Account, String> migrateData(CredentialsRequest request, ClientInfo clientInfo) {
        return List.ofAll(request.getAccounts())
                .map(account -> new Tuple2<>(account, account.getAccountNumber()))
                .toJavaMap(acc -> acc);
    }
}
