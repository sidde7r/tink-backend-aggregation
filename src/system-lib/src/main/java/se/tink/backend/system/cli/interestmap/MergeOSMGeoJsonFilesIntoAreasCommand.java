package se.tink.backend.system.cli.interestmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sourceforge.argparse4j.inf.Namespace;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

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
 *
 */
public class MergeOSMGeoJsonFilesIntoAreasCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final LogUtils log = new LogUtils(MergeOSMGeoJsonFilesIntoAreasCommand.class);

    private static final ImmutableSet<Long> RELATION_IDS_TO_IGNORE = ImmutableSet.of(
            398021L, // stockholm
            935611L, // göteborg
            935416L, // malmö
            935560L, // helsingborg
            5525145L, 5525146L, 5525147L, 5525148L, 5525149L, 5525150L, 5525151L, 5525152L, 5525153L, 5525154L, 5525155L, 5525156L, 5525157L, 5525158L, 5525159L, 5525160L, 5525161L, 5525162L, 5525163L, 5525164L, 5525165L, 5525166L, 5525167L, 5525168L, 5525169L, 5525170L, 5525171L, 5525172L, 5525173L, 5525174L, 5525175L, 5525176L, 5525177L, 5525178L, 5525179L, 5525180L, 5525181L, 5525182L, 5525183L, 5525184L, 5525185L, 5525186L // hbg level 9
    );

    private static final ImmutableSet<Long> LEVEL_9_RELATION_IDS_TO_INCLUDE = ImmutableSet.of(
            5689303L, 5689375L, 5690815L, 5690852L, 5691336L, 5691404L, 5691543L, 5695372L, 5695412L, 5695415L, 5695432L, 5695996L, 5701014L, 5730716L, // sthlm level 9
            388997L, 389859L, 1517089L, 1517093L, 1517108L, 1517115L, 1517232L, 1517298L, 1520467L, 1520595L, // gbg level 9
            3049245L, 3049246L, 3049247L, 3049248L, 3049249L // malmö level 9
    );

    public MergeOSMGeoJsonFilesIntoAreasCommand() {
        super("merge-geojson-into-areas", "Builds a GeoJSON file for Interest Map");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext context) throws Exception {

        String file1 = System.getProperty("geojsonFile1", null);
        String file2 = System.getProperty("geojsonFile2", null);
        String outputFile = System.getProperty("outputFile", null);

        if (Strings.isNullOrEmpty(file1) || !new File(file1).exists() ||
                Strings.isNullOrEmpty(file2) || !new File(file2).exists() ||
                Strings.isNullOrEmpty(outputFile)) {

            log.error("Need existing geojsonFile1 and geojsonFile2, also need outputFile");
            return;
        }

        FeatureCollection collection = MAPPER.readValue(new File(file1), FeatureCollection.class);
        FeatureCollection collection2 = MAPPER.readValue(new File(file2), FeatureCollection.class);

        FeatureCollection collectionToSave = new FeatureCollection();

        collectionToSave.addAll(getFeatures(collection));
        collectionToSave.addAll(getFeatures(collection2));

        Files.write(MAPPER.writeValueAsString(collectionToSave), new File(outputFile), Charsets.UTF_8);
    }

    private List<Feature> getFeatures(FeatureCollection collection) {
        List<Feature> features = Lists.newArrayList();
        for (Feature feature : collection.getFeatures()) {

            if (!Objects.equals("relation", feature.getProperties().get("type"))) {
                log.debug("Skipping non-relation feature");
                continue;
            }

            if (feature.getProperties().containsKey("id")) {
                Object id = feature.getProperties().get("id");

                if (RELATION_IDS_TO_IGNORE.contains(Long.parseLong(id.toString()))) {
                    continue;
                }

                Map<String, String> tags = (Map<String, String>) feature.getProperties().get("tags");
                if ("9".equals(tags.get("admin_level"))) {
                    if (!LEVEL_9_RELATION_IDS_TO_INCLUDE.contains(Long.parseLong(id.toString()))) {
                        // only include level 9 areas of those that were picked out
                        continue;
                    }
                }

                features.add(feature);
            }
        }
        return features;
    }
}
