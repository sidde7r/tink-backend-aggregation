package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;

public class ClusterConnectivityController {

    private ControllerWrapperProvider controllerWrapperProvider;

    @Inject
    public ClusterConnectivityController(ControllerWrapperProvider controllerWrapperProvider) {
        this.controllerWrapperProvider = controllerWrapperProvider;
    }

    public Response checkConnectivity(String clusterId) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clusterId);
        return controllerWrapper.checkConnectivity();
    }
}
