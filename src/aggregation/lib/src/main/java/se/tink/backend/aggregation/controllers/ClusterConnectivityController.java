package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientHandlerException;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;

public class ClusterConnectivityController {

    private ControllerWrapperProvider controllerWrapperProvider;

    @Inject
    public ClusterConnectivityController(ControllerWrapperProvider controllerWrapperProvider) {
        this.controllerWrapperProvider = controllerWrapperProvider;
    }

    public void checkConnectivity(String clusterId) throws AggregationControllerNotReachable {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clusterId);

        try {
            controllerWrapper.checkConnectivity();
        } catch (ClientHandlerException e) {
            throw new AggregationControllerNotReachable();
        }
    }
}
