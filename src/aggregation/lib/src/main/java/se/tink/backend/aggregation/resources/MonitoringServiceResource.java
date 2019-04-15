package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.controllers.AggregationControllerNotReachable;
import se.tink.backend.aggregation.controllers.ClusterConnectivityController;

public class MonitoringServiceResource implements MonitoringService {
    private ClusterConnectivityController clusterConnectivityController;

    @Inject
    public MonitoringServiceResource(ClusterConnectivityController clusterConnectivityController) {
        this.clusterConnectivityController = clusterConnectivityController;
    }

    @Override
    public Response checkConnectivity() {
        try {
            clusterConnectivityController.checkConnectivity();
        } catch (AggregationControllerNotReachable e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @Override
    public Response checkConnectivity(String clusterId) {
        try {
            clusterConnectivityController.checkConnectivity(clusterId);
        } catch (AggregationControllerNotReachable e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }
}
