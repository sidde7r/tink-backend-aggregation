package se.tink.backend.system.cli.interestmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.sourceforge.argparse4j.inf.Namespace;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.AggregatedAreaLoanDataRepository;
import se.tink.backend.common.repository.cassandra.UserCoordinatesRepository;
import se.tink.backend.core.UserCoordinates;
import se.tink.backend.core.interests.AggregatedAreaLoanData;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.interestmap.models.Area;
import se.tink.backend.system.cli.interestmap.models.Bank;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * A geojson file can be used to encode geographic data structures.
 * In Google Maps (which we use for Interest Map) we can apply a geojson file to layer data ontop of the map.
 *
 * A geojson file looks like this:
 * <code>
 * {
 *   "type": "Feature",
 *   "geometry": {
 *     "type": "Point",
 *     "coordinates": [125.6, 10.1]
 *   },
 *   "properties": {
 *     "name": "Dinagat Islands"
 *   }
 * }
 * </code>
 *
 * More information can be found on http://geojson.org
 */
public class BuildGeoJsonCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final LogUtils log = new LogUtils(BuildGeoJsonCommand.class);
    private static final int DEFAULT_THRESHOLD_NUM_USERS = 10;

    private static final Function<UserCoordinates, String> USER_COORDINATES_TO_AREA_ID = uc -> UUIDUtils
            .toTinkUUID(uc.getAreaId());

    private static final Predicate<UserCoordinates> HAS_ASSIGNED_AREA_ID = uc -> uc.getAreaId() != null;

    private int userThreshold;

    public BuildGeoJsonCommand() {
        super("build-interest-map-geojson", "Builds a GeoJSON file for Interest Map");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext context) throws Exception {

        String command = System.getProperty("command", null);

        if (Strings.isNullOrEmpty(command)) {
            log.error("Need command input (can be either loanGeography or userGeography)");
            return;
        }

        if ("userGeography".equals(command)) {
            runUserGeography(context);
        } else if ("loanGeography".equals(command)) {
            runLoanGeography(context);
        }
    }

    private void runUserGeography(ServiceContext context) throws IOException {

        String areaFile = System.getProperty("areaFile", null);
        String outputFile = System.getProperty("outputFile", null);

        if (Strings.isNullOrEmpty(areaFile) || !new File(areaFile).exists() || Strings.isNullOrEmpty(outputFile)) {
            log.error("Need existing areaFile and outputFile");
            return;
        }

        UserCoordinatesRepository userCoordinatesRepository = context.getRepository(UserCoordinatesRepository.class);

        ListMultimap<String, UserCoordinates> usersByAreaId = FluentIterable.from(userCoordinatesRepository.findAll())
                .filter(HAS_ASSIGNED_AREA_ID)
                .index(USER_COORDINATES_TO_AREA_ID);

        FeatureCollection allFeatures = MAPPER.readValue(new File(areaFile),
                FeatureCollection.class);

        FeatureCollection outputFeatures = new FeatureCollection();

        for (Feature feature : allFeatures) {
            String tinkId = (String) feature.getProperties().get("tinkId");
            if (usersByAreaId.containsKey(tinkId)) {
                feature.getProperties().put("numUsers", usersByAreaId.get(tinkId).size());
                outputFeatures.add(feature);
            }
        }

        Files.write(MAPPER.writeValueAsString(outputFeatures), new File(outputFile), Charsets.UTF_8);
    }

    private void runLoanGeography(ServiceContext context) throws IOException {
        String areaFile = System.getProperty("areaFile", null);
        String outputFile = System.getProperty("outputFile", null);
        this.userThreshold = Integer.getInteger("userThreshold", DEFAULT_THRESHOLD_NUM_USERS);

        if (Strings.isNullOrEmpty(areaFile) || !new File(areaFile).exists() ||
                Strings.isNullOrEmpty(outputFile)) {

            log.error("Need existing areaFile and output file");
            return;
        }

        Map<UUID, Feature> featuresById = readAreas(areaFile);

        ListMultimap<UUID, AggregatedAreaLoanData> aggregatedLoanByPostalCode = createAggregatedLoanMap(
                context.getRepository(AggregatedAreaLoanDataRepository.class));

        FeatureCollection featureCollection = buildGeoJsonObject(featuresById, aggregatedLoanByPostalCode);

        Files.write(MAPPER.writeValueAsString(featureCollection), new File(outputFile), Charsets.UTF_8);
    }

    private Map<UUID, Feature> readAreas(String areaFile) throws IOException {
        FeatureCollection features = MAPPER.readValue(new File(areaFile),
                FeatureCollection.class);

        Map<UUID, Feature> featureById = Maps.newHashMap();
        for (Feature feature : features) {
            Object tinkId = feature.getProperties().get("tinkId");
            featureById.put(UUIDUtils.fromTinkUUID((String) tinkId), feature);
        }

        return featureById;
    }

    private ListMultimap<UUID, AggregatedAreaLoanData> createAggregatedLoanMap(
            AggregatedAreaLoanDataRepository repository) {

        return FluentIterable.from(repository.findAll()).index(AggregatedAreaLoanData::getAreaId);
    }

    private FeatureCollection buildGeoJsonObject( Map<UUID, Feature> featuresById,
            ListMultimap<UUID, AggregatedAreaLoanData> aggregatedLoanByPostalCode) {

        FeatureCollection collection = new FeatureCollection();

        for (UUID areaId : featuresById.keySet()) {

            if (!aggregatedLoanByPostalCode.containsKey(areaId)) {
                continue;
            }

            Area area = createAreaPropertyIfApplicable(aggregatedLoanByPostalCode.get(areaId));

            if (area != null) {

                Feature feature = cleanFeature(featuresById.get(areaId));
                feature.getProperties().put("area", area);

                collection.add(feature);
            }
        }

        return collection;
    }

    private Feature cleanFeature(Feature feature) {
        Object tinkId = feature.getProperties().get("tinkId");
        Map<String, String> tags = (Map<String, String>)feature.getProperties().get("tags");
        Map<String, Object> properties = Maps.newHashMap();

        Feature cleanFeature = new Feature();
        cleanFeature.setGeometry(feature.getGeometry());
        cleanFeature.setId((String)tinkId);
        cleanFeature.setProperties(properties);

        cleanFeature.setProperty("displayName", tags.get("name"));

        return cleanFeature;
    }

    private Area createAreaPropertyIfApplicable(List<AggregatedAreaLoanData> areaLoanData) {

        Area area = new Area();

        if (areaLoanData.size() > 0) {
            Preconditions.checkArgument(Objects.equals(areaLoanData.get(0).getBank(), "all"));

            if (areaLoanData.get(0).getNumUsers() < userThreshold) {
                // Don't include area if there is not enough data
                return null;
            }

            List<Bank> banks = Lists.newArrayList();

            area.setAvgInterestRate(areaLoanData.get(0).getAvgInterest());
            area.setAvgLoanBalance(Math.round(areaLoanData.get(0).getAvgBalance()));
            area.setNumLoans(areaLoanData.get(0).getNumLoans());
            area.setNumUsers(areaLoanData.get(0).getNumUsers());

            if (areaLoanData.size() > 1) {
                for (int i = 1; i < areaLoanData.size(); i++) {
                    if (areaLoanData.get(i).getNumUsers() >= userThreshold) {
                        Bank bank = new Bank();
                        bank.setDisplayName(areaLoanData.get(i).getBankDisplayName());
                        bank.setAvgInterestRate(areaLoanData.get(i).getAvgInterest());
                        bank.setNumLoans(areaLoanData.get(i).getNumLoans());
                        bank.setNumUsers(areaLoanData.get(i).getNumUsers());
                        banks.add(bank);
                    }
                }

                area.setBanks(banks);
            }
        }

        return area;
    }
}
