package se.tink.backend.system.cli.seeding;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.sql.Connection;
import java.sql.DriverManager;
import net.sourceforge.argparse4j.inf.Namespace;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CleanDatabaseCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(CleanDatabaseCommand.class);

    public CleanDatabaseCommand() {
        super("clean-database", "Drops the entire database");
    }

    @Override
    public void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace, ServiceConfiguration configuration,
            Injector injector, ServiceContext context) throws Exception {
        cleanDatabase(context);
    }

    public static void cleanDatabase(ServiceContext context) {
        log.info("Dropping database...");
        try {
            DatabaseConfiguration databaseConfiguration = context.getConfiguration().getDatabase();

            Class.forName(databaseConfiguration.getDriverClass());

            Connection connection = DriverManager.getConnection(databaseConfiguration.getUrl(),
                    databaseConfiguration.getUsername(), databaseConfiguration.getPassword());

            String url = databaseConfiguration.getUrl();
            String[] urlComponents = url.split("/");
            String dbName = urlComponents[urlComponents.length - 1].split("\\?")[0];
            log.info("\tdatabase name: " + dbName);

            connection.prepareStatement("DROP SCHEMA " + dbName).executeUpdate();
            connection.prepareStatement("CREATE SCHEMA " + dbName).executeUpdate();

            connection.close();

            log.info("Deleting Elasticsearch indices...");

            try {
                SearchProxy.getInstance().getClient().admin().indices().delete(new DeleteIndexRequest("transactions"))
                        .actionGet();
            } catch (Exception e) {
                // NOOP.
            }

            try {
                SearchProxy.getInstance().getClient().admin().indices().delete(new DeleteIndexRequest("merchants"))
                        .actionGet();
            } catch (Exception e) {
                // NOOP.
            }

            log.info("Done dropping database.");
        } catch (Exception e) {
            log.error("Could not clean database", e);
        }
    }
}
