package se.tink.backend.system.cli.location;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class GeoLookupUserLocationCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(GeoLookupUserLocationCommand.class);

    public GeoLookupUserLocationCommand() {
        super("geo-lookup-user-location", "Lookup the location of users based on their IP address");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        UserDemographicsRepository userDemographicsRepository = serviceContext
                .getRepository(UserDemographicsRepository.class);

        ImmutableMap<String, UserDemographics> userDemographicsByUserId = Maps.uniqueIndex(
                userDemographicsRepository.findAll(), UserDemographics::getUserId);

        DatabaseReader marketsLookupDatabase = new DatabaseReader.Builder(new File("data/GeoIP2-City.mmdb")).build();

        List<List<String>> lines = StringUtils.readLines(new File("ip.txt"));

        int count = 0;

        for (List<String> line : lines) {
            String userId = line.get(0);

            UserDemographics userDemographics = userDemographicsByUserId.get(userId);

            if (userDemographics == null) {
                continue;
            }

            String ipAddress = line.get(1);

            try {
                CityResponse result = marketsLookupDatabase.city(InetAddress.getByName(ipAddress));

                log.info(userId + ":" + ipAddress + " -> " + result.getCity() + ", " + result.getCountry());

                userDemographics.setCountry(result.getCountry().getName());
                userDemographics.setCity(result.getCity().getName());

                userDemographicsRepository.save(userDemographics);

                count++;
            } catch (Exception e) {
                log.error("Could not lookup ip: " + ipAddress, e);
            }
        }

        log.info("Found location for " + count + " users");
    }
}
