package se.tink.backend.system.cli.interestmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.UserCoordinatesRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserCoordinates;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.libraries.uuid.UUIDUtils;

public class AssignUsersAreasCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final LogUtils log = new LogUtils(AssignUsersAreasCommand.class);

    private UserCoordinatesRepository userCoordinateRepository;

    public AssignUsersAreasCommand() {
        super("assign-users-areas", "Assign areas to users based on their coordinates");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        userCoordinateRepository = serviceContext.getRepository(UserCoordinatesRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        String file1 = System.getProperty("geojsonFile", null);

        if (Strings.isNullOrEmpty(file1) || !new File(file1).exists()) {

            log.error("Need existing geojsonFile");
            return;
        }

        FeatureCollection featureCollection = MAPPER.readValue(new File(file1), FeatureCollection.class);

        final LinkedList<TinkPolygon> polygons = populateTinkPolygons(featureCollection);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        assignAreaToUser(polygons, user);
                    } catch (Exception e) {
                        log.warn(user.getId(), "Caught exception while processing user.", e);
                    }
                });
    }

    private void assignAreaToUser(LinkedList<TinkPolygon> polygons, User user) {

        UserCoordinates coordinates = userCoordinateRepository.findOneByUserId(UUIDUtils.fromTinkUUID(user.getId()));

        if (coordinates == null || coordinates.getLatitude() == null || coordinates.getLongitude() == null) {
            return;
        }

        Iterator<TinkPolygon> it = polygons.iterator();
        while (it.hasNext()) {
            TinkPolygon polygon = it.next();
            if (Polygons.isCoordinatesInside(polygon, coordinates.getLatitude(), coordinates.getLongitude())) {

                coordinates.setAreaId(UUIDUtils.fromTinkUUID(polygon.getAreaId()));
                userCoordinateRepository.save(coordinates);

                // move this current polygon to front in order to quicker find it next time
                it.remove();
                polygons.addFirst(polygon);
                break;
            }
        }
    }

    private LinkedList<TinkPolygon> populateTinkPolygons(FeatureCollection featureCollection) {
        LinkedList<TinkPolygon> linkedList = Lists.newLinkedList();

        for (Feature feature : featureCollection.getFeatures()) {
            Map<String, Object> properties = feature.getProperties();

            String id = (String) properties.get("tinkId");

            if (id == null) {
                continue;
            }

            if (feature.getGeometry() instanceof MultiPolygon) {

                MultiPolygon multiPolygon = (MultiPolygon) feature.getGeometry();
                for (List<List<LngLatAlt>> rings : multiPolygon.getCoordinates()) {
                    Polygon polygon = new Polygon();
                    for (int i = 0; i < rings.size(); i++) {
                        if (i == 0) {
                            // exterior ring
                            polygon.setExteriorRing(rings.get(i));
                        } else {
                            // interior ring
                            polygon.addInteriorRing(rings.get(i));
                        }
                    }
                    linkedList.add(new TinkPolygon(id, polygon));
                }
            } else if (feature.getGeometry() instanceof Polygon) {
                Polygon polygon = (Polygon) feature.getGeometry();
                linkedList.add(new TinkPolygon(id, polygon));
            } else {
                log.warn("Found unhandled geometry: " + feature.getGeometry().getClass().getName());
            }
        }

        return linkedList;
    }
}
