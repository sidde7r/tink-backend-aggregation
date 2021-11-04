package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.controllers.AggregationControllerNotReachable;
import se.tink.backend.aggregation.controllers.ClusterConnectivityController;

public class MonitoringServiceResource implements MonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringServiceResource.class);

    private ClusterConnectivityController clusterConnectivityController;

    @Inject
    public MonitoringServiceResource(ClusterConnectivityController clusterConnectivityController) {
        this.clusterConnectivityController = clusterConnectivityController;
        LOGGER.info("[MonitoringServiceResource] Successfully initialised");
    }

    @Override
    public Response checkConnectivity() {
        try {
            LOGGER.info("[MonitoringServiceResource] checkConnectivity method called");
            clusterConnectivityController.checkConnectivity();
        } catch (AggregationControllerNotReachable e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("[MonitoringServiceResource] checkConnectivity method succeeded");
        return Response.ok().build();
    }

    @Override
    public Response checkConnectivity(String clusterId) {
        try {
            LOGGER.info(
                    "[MonitoringServiceResource] checkConnectivity with clusterId method called");
            clusterConnectivityController.checkConnectivity(clusterId);
        } catch (AggregationControllerNotReachable e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info(
                "[MonitoringServiceResource] checkConnectivity with clusterId method succeeded");
        return Response.ok().build();
    }
}
