package se.tink.backend.system.cli.analytics;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.CassandraUserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.utils.CassandraUserDemographicsConverter;
import se.tink.backend.core.CassandraUserDemographics;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CopyUserDemographicsToCassandraCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(CopyUserDemographicsToCassandraCommand.class);

    public CopyUserDemographicsToCassandraCommand() {
        super("copy-user-demographics-to-cassandra", "Copy user demographics from MySQL to Cassandra.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Copying user demographics.");
        
        final UserDemographicsRepository userDemographicsRepository = serviceContext
                .getRepository(UserDemographicsRepository.class);
        final CassandraUserDemographicsRepository cassandraUserDemographicsRepository = serviceContext
                .getRepository(CassandraUserDemographicsRepository.class);
        
        // Expect all of these to fit in memory.
        List<UserDemographics> mysqlData = userDemographicsRepository.findAll();
        
        cassandraUserDemographicsRepository.truncate();
        
        for (UserDemographics row : mysqlData) {
            log.info(row.getUserId(), "Copying row....");
            CassandraUserDemographics converted = CassandraUserDemographicsConverter.toCassandra(row);
            
            // Saving one by one to not hit the max size of prepared statements. Could potentially batch these
            // a little, but the data is so small it's not worth it.
            cassandraUserDemographicsRepository.save(converted);
        }

        log.info(String.format("Copied %d users.", mysqlData.size()));
    }

}
