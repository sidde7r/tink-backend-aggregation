package se.tink.backend.system.cli.seeding;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.persistence.Table;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.cassandra.core.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedRepositoryConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Notification;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.seeding.search.RebuildSearchIndicesCommand;

public class SeedDatabaseCommand extends ServiceContextCommand<ServiceConfiguration> {

    public SeedDatabaseCommand() {
        super("seed-database", "Seed the database with all required data.");
    }

    private static final String RESET_DISTRIBUTED_DATABASE = "resetDistributedDatabase";

    private static boolean systemPropertyIsExplicitlySetToFalse(String property) {
        boolean isSet = System.getProperty(RESET_DISTRIBUTED_DATABASE) != null;
        return isSet && !Boolean.getBoolean(property);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        try {
            // Seed initial database structures.
            if (System.getProperty("seedOnly") != null) {
                DatabaseSeeder seeder = DatabaseSeeder.getInstance(serviceContext);
                String seedOnly = System.getProperty("seedOnly").toLowerCase();
                switch (seedOnly) {
                case "category":
                    seeder.seedCategories();
                    break;
                case "currency":
                    seeder.seedCurrencies();
                    break;
                case "geography":
                    seeder.seedGeography();
                    break;
                case "market":
                    seeder.seedMarkets();
                    break;
                case "products":
                    seeder.seedProducts();
                    break;
                case "providers":
                    seeder.seedProviders(configuration.isDevelopmentMode());
                    break;
                default:
                    break;
                }
            } else {
                if (!systemPropertyIsExplicitlySetToFalse(RESET_DISTRIBUTED_DATABASE)) {
                    // Default is to do this unless explicitly set to false.
                    resetDistributedDatabase(serviceContext);
                } else {
                    CreateDistributedTablesCommand.create(serviceContext);
                }

                DatabaseSeeder seeder = DatabaseSeeder.getInstance(serviceContext);
                seeder.seedCategories();
                seeder.seedCurrencies();
                seeder.seedGeography();
                seeder.seedMarkets();
                seeder.seedProducts();
                seeder.seedProviders(configuration.isDevelopmentMode());
                seeder.seedConsents();

                createIndices(serviceContext);
                executeAlters(serviceContext);

                // Rebuild the search indices.

                final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
                RebuildSearchIndicesCommand.rebuildIndices(serviceContext, userRepository.streamAll());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createIndices(ServiceContext serviceContext) throws Exception {
        DatabaseConfiguration databaseConfiguration = serviceContext.getConfiguration().getDatabase();

        String url = databaseConfiguration.getUrl();
        String[] urlComponents = url.split("/");
        String dbName = urlComponents[urlComponents.length - 1].split("\\?")[0];

        Class.forName(databaseConfiguration.getDriverClass());

        Connection connection = DriverManager.getConnection(databaseConfiguration.getUrl(),
                databaseConfiguration.getUsername(), databaseConfiguration.getPassword());

        try {
            for (String query : Files.readLines(new File("data/indices.sql"), Charsets.UTF_8)) {
                query = query.replace("tink", dbName);
                try {
                    connection.prepareStatement(query).executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException("Could execute query: " + query, e);
                }
            }
        } finally {
            connection.close();
        }
    }

    private void executeAlters(ServiceContext serviceContext) throws Exception {
        DatabaseConfiguration databaseConfiguration = serviceContext.getConfiguration().getDatabase();

        String url = databaseConfiguration.getUrl();
        String[] urlComponents = url.split("/");
        String dbName = urlComponents[urlComponents.length - 1].split("\\?")[0];

        Class.forName(databaseConfiguration.getDriverClass());

        Connection connection = DriverManager.getConnection(databaseConfiguration.getUrl(),
                databaseConfiguration.getUsername(), databaseConfiguration.getPassword());

        try {
            // We need to execute this since Spring Data/Hibernate can't be told the order of a primary composite key.
            // See http://stackoverflow.com/a/28004990/260805.
            connection.prepareStatement(
                    String.format("ALTER TABLE %s.%s DROP PRIMARY KEY, ADD PRIMARY KEY (`userid`(191),`id`(191));",
                            dbName, Notification.class.getAnnotation(Table.class).name())).executeUpdate();
        } finally {
            connection.close();
        }
    }

    private void resetDistributedDatabase(ServiceContext serviceContext) {

        // Drop (ie. delete all tables) and create the keyspace

        CassandraOperations cassandraOperations = serviceContext.getCassandraOperations();
        DropKeyspaceSpecification dropKeyspaceSpec = DropKeyspaceSpecification.dropKeyspace("tink").ifExists();
        cassandraOperations.execute(dropKeyspaceSpec);
        cassandraOperations.execute(DistributedRepositoryConfiguration.keyspaceSpec);

        // Create all tables

        CreateDistributedTablesCommand.create(serviceContext);
    }
}
