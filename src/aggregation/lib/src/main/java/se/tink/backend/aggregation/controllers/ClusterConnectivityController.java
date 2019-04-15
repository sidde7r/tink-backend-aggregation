package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientHandlerException;
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
        Set<String> clusterIds = controllerWrapperProvider.getClusterIds();
        boolean anyClusterFailed = false;

        for(String clusterId : clusterIds) {
            try {
                checkConnectivity(clusterId);
            } catch (AggregationControllerNotReachable e) {
                log.error("Connection to %s cluster failed", clusterId);
                anyClusterFailed = true;
            }
        }

        if(anyClusterFailed) {
            throw new AggregationControllerNotReachable();
        }
    }

    public void checkConnectivity(String clusterId) throws AggregationControllerNotReachable {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clusterId);

        try {
            controllerWrapper.checkConnectivity();
        } catch (ClientHandlerException e) {
            log.error("Connection to %s cluster failed", clusterId);
            throw new AggregationControllerNotReachable();
        }
    }
}
