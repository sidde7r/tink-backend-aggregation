package se.tink.backend.system.cli.seeding;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.PostalCodeArea;
import se.tink.backend.system.cli.ServiceContextCommand;

public class DownloadCoordinatesForPostalCodeAreasCommand extends ServiceContextCommand<ServiceConfiguration> {

    public static final boolean cleanUpOutsideSwedenBox = false;

    //Very naive approach
    private static final double NORTH_MOST_LAT = 69.225860;
    private static final double SOUTH_MOST_LAT = 55.180257;

    private static final double EAST_MOST_LNG = 24.354873;
    private static final double WEST_MOST_LNG = 10.797745;

    public DownloadCoordinatesForPostalCodeAreasCommand() {
        super("download-coords-to-postals", "Seeds PostalCodeAreas with coordinates from Google Searches");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        PostalCodeAreaRepository postalCodeAreaRepository = serviceContext.getRepository(PostalCodeAreaRepository.class);

        if (cleanUpOutsideSwedenBox) {
            cleanUp(postalCodeAreaRepository);
        } else {
            seedDatabase(postalCodeAreaRepository);
        }

    }

    private void cleanUp(PostalCodeAreaRepository postalCodeAreaRepository) {
        List<PostalCodeArea> postalCodeAreas = postalCodeAreaRepository.findAll();

        for(PostalCodeArea area : postalCodeAreas) {
            if (!area.getCountry().equals("Sweden")) {
                continue;
            } else if (area.getLongitude() == null && area.getLatitude() == null) {
                continue;
            }

            if (area.getLatitude() > NORTH_MOST_LAT || area.getLatitude() < SOUTH_MOST_LAT ||
                    area.getLongitude() > EAST_MOST_LNG || area.getLongitude() < WEST_MOST_LNG) {
                area.setLatitude(null);
                area.setLongitude(null);
                postalCodeAreaRepository.save(area);
            }
        }
    }

    private void seedDatabase(PostalCodeAreaRepository postalCodeAreaRepository) throws Exception {

        GooglePlacesSearcher searcher = new GooglePlacesSearcher();

        List<PostalCodeArea> postalCodeAreas = postalCodeAreaRepository.findAll();

        for(PostalCodeArea area : postalCodeAreas) {
            if (!area.getCountry().equals("Sweden")) {
                continue;
            } else if (area.getLongitude() != null && area.getLatitude() != null) {
                continue;
            }

            Coordinate coords = searcher.geocode(area.getPostalCode()+", "+area.getCity(), "sv_SE");
            if (coords != null) {
                if (coords.getLatitude() < NORTH_MOST_LAT && coords.getLatitude() > SOUTH_MOST_LAT &&
                        coords.getLongitude() < EAST_MOST_LNG && coords.getLongitude() > WEST_MOST_LNG) {

                    area.setLatitude(coords.getLatitude());
                    area.setLongitude(coords.getLongitude());
                    postalCodeAreaRepository.save(area);
                } else {
                    System.out.println("coords.getLatitude() = " + coords.getLatitude());
                    System.out.println("coords.getLongitude() = " + coords.getLongitude());
                }
            }

            Thread.sleep(300);
        }
    }
}
