package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;

public class ClusterConnectivityController {
    private static final Logger log = LoggerFactory.getLogger(ClusterConnectivityController.class);

    private ControllerWrapperProvider controllerWrapperProvider;

    @Inject
    public ClusterConnectivityController(ControllerWrapperProvider controllerWrapperProvider) {
        this.controllerWrapperProvider = controllerWrapperProvider;
    }

    public void checkConnectivity() throws AggregationControllerNotReachable {
        log.info("Starting check of connectivity to customer facing environments");

        Set<String> clusterIds = controllerWrapperProvider.getClusterIds();
        log.info(
                "Checking connectivity to the following environments: {}",
                String.join(",", clusterIds));

        for (String clusterId : clusterIds) {
            checkConnectivity(clusterId);
        }

        log.info("Finished checking connectivity to environments");
    }

    public void checkConnectivity(String clusterId) throws AggregationControllerNotReachable {
        log.info("Checking connectivity to: {}", clusterId);

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clusterId);

        try {
            controllerWrapper.checkConnectivity();
            log.info("Successfully sent request to: {}", clusterId);
        } catch (RuntimeException e) {
            log.error("Connection to {} cluster failed : {}", clusterId, e.getMessage());
            throw new AggregationControllerNotReachable();
        }
    }
}
