package se.tink.backend.system.cli.interestmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Maps;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

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
public class MergeOSMAndDistrictCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final LogUtils log = new LogUtils(MergeOSMAndDistrictCommand.class);

    public MergeOSMAndDistrictCommand() {
        super("merge-osm-and-district", "Builds a GeoJSON file for Interest Map");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext context) throws Exception {

        String osmFile = System.getProperty("osmFile", null);
        String districtFile = System.getProperty("districtFile", null);
        String outputFile = System.getProperty("outputFile", null);

        if (Strings.isNullOrEmpty(osmFile) || !new File(osmFile).exists() ||
                Strings.isNullOrEmpty(districtFile) || !new File(districtFile).exists() ||
                Strings.isNullOrEmpty(outputFile)) {

            log.error("Need existing osmFile and districtFile, also need outputFile");
            return;
        }

        FeatureCollection osm = MAPPER.readValue(new File(osmFile), FeatureCollection.class);
        FeatureCollection district = MAPPER.readValue(new File(districtFile), FeatureCollection.class);

        FeatureCollection collectionToSave = new FeatureCollection();

        collectionToSave.addAll(getOSMFeatures(osm));
        collectionToSave.addAll(getDistrictFeatures(district));

        Files.write(MAPPER.writeValueAsString(collectionToSave), new File(outputFile), Charsets.UTF_8);
    }

    private List<Feature> getDistrictFeatures(FeatureCollection collection) {
        List<Feature> features = Lists.newArrayList();
        for (Feature feature : collection.getFeatures()) {

            Map<String, Object> properties = feature.getProperties();
            Map<String, String> tags = Maps.newHashMap();

            tags.put("name", (String)properties.get("DISTRNAMN"));

            properties.put("id", String.format("distr/%s", feature.getProperties().get("DISTRKOD")));
            properties.put("tags", tags);
            properties.put("tinkId", StringUtils.generateUUID());

            features.add(feature);
        }
        return features;
    }

    private List<Feature> getOSMFeatures(FeatureCollection collection) {
        List<Feature> features = Lists.newArrayList();
        for (Feature feature : collection.getFeatures()) {

            Map<String, String> tags = (Map<String, String>) feature.getProperties().get("tags");

            if (!tags.containsKey("name")) {
                // Some areas were missing the name tag, if official_name or name:sv exists, take that.
                if (tags.containsKey("official_name")) {
                    tags.put("name", tags.get("official_name"));
                } else if (tags.containsKey("name:sv")) {
                    tags.put("name", tags.get("name:sv"));
                }
            }

            feature.getProperties().put("tinkId", StringUtils.generateUUID());
            features.add(feature);
        }
        return features;
    }
}
