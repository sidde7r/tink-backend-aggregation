package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.avanza;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class Sanitizing_v1_v2 extends DataVersionMigration {

    @Override
    public int getMigrationFromVersion() {
        return 1;
    }

    @Override
    protected Map<Account, String> migrateData(CredentialsRequest request, ClientInfo clientInfo) {
        return List.ofAll(request.getAccounts())
                .map(
                        account ->
                                new Tuple2<>(
                                        account,
                                        account.getBankId().replaceAll("[^A-Za-z0-9]", "")))
                .toJavaMap(acc -> acc);
    }
}
