package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.controllers.AggregationControllerNotReachable;
import se.tink.backend.aggregation.controllers.ClusterConnectivityController;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class MonitoringServiceResource implements MonitoringService {
    private ClusterConnectivityController clusterConnectivityController;

    @Inject
    public MonitoringServiceResource(ClusterConnectivityController clusterConnectivityController) {
        this.clusterConnectivityController = clusterConnectivityController;
    }

    @Override
    public String checkConnectivity(String clusterId) {
        try {
            clusterConnectivityController.checkConnectivity(clusterId);
        } catch (AggregationControllerNotReachable e) {
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return "OK";
    }
}
