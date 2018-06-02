package se.tink.backend.system.cli.location;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.params.HttpConnectionParams;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.common.repository.mysql.main.CityCoordinateRepository;
import se.tink.backend.core.CityCoordinate;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class SeedCityCoordinatesCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final String DEFAULT_LOCALE = "sv_SE";

    private static final LogUtils log = new LogUtils(SeedCityCoordinatesCommand.class);
    private GooglePlacesSearcher googleSearcher;
    private int failed;
    private int success;

    public SeedCityCoordinatesCommand() {
        super("seed-city-coordinates", "Seeds city coordnates table in database.");

        googleSearcher = new GooglePlacesSearcher();
        googleSearcher.setHttpParam(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
        googleSearcher.setHttpParam(HttpConnectionParams.SO_TIMEOUT, 10000);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        CityCoordinateRepository repository = serviceContext.getRepository(CityCoordinateRepository.class);

        File postalCodeFile = new File("data/seeding/cities-postal-area-codes--2015-02-16.txt");
        File facebookFile = new File("data/seeding/cities-facebook-profiles--most-common--2015-02-16.txt");
        File merchantsFile = new File("data/seeding/cities-merchants--2015-02-16.txt");

        List<String> postalCodeLines = Files.readLines(postalCodeFile, Charsets.UTF_8);
        List<String> facebookLines = Files.readLines(facebookFile, Charsets.UTF_8);
        List<String> merchantsLines = Files.readLines(merchantsFile, Charsets.UTF_8);

        failed = 0;
        success = 0;

        Map<String, CityCoordinate> coordinates = fetchFromGoogle(postalCodeLines, facebookLines, merchantsLines);

        repository.deleteAll();
        repository.save(coordinates.values());
    }

    private Map<String, CityCoordinate> fetchFromGoogle(Iterable<String>... sources) throws Exception {
        Map<String, CityCoordinate> coordinates = Maps.newHashMap();
        Set<String> alreadyLookedUp = Sets.newHashSet();

        for (Iterable<String> source : sources) {

            for (String city : source) {

                if (alreadyLookedUp.contains(city)) {
                    log.debug("Already searched for " + city + ".");
                    continue;
                }

                CityCoordinate cityCoord = null;
                try {
                    cityCoord = googleSearcher.geocodeCity(city, DEFAULT_LOCALE);
                } catch (Exception e) {
                    log.error("Caught exception when fetching " + city + ".", e);
                }

                if (cityCoord == null || Strings.isNullOrEmpty(cityCoord.getCity())) {
                    failed++;
                    log.info("Failed fetching: " + city);
                } else {
                    success++;
                    alreadyLookedUp.add(city);

                    String key = cityCoord.getCity().toLowerCase() + ", " + cityCoord.getCountry().toLowerCase();
                    if (!coordinates.containsKey(key)) {
                        coordinates.put(key, cityCoord);
                        log.debug("Adding " + key + " to database (this search string: " + city + ").");
                    } else {
                        log.debug("Already found " + key + " in database (this search string: " + city + ").");
                    }
                }

                if ((failed + success) % 50 == 0) {
                    log.info(String.format("%d succeed, %d failed", success, failed));
                }

                // max 5 requests per second + buffer
                Thread.sleep(300);
            }
        }

        return coordinates;
    }
}
