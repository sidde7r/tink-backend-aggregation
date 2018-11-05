package se.tink.backend.aggregation.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;

@Path("/creditsafe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Deprecated
public interface CreditSafeService {
    @DELETE
    @Path("consumermonitoring")
    @TeamOwnership(Team.INTEGRATION)
    void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo);

    @POST
    @Path("consumermonitoring")
    @TeamOwnership(Team.INTEGRATION)
    Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo);

    @GET
    @Path("consumermonitoring/portfolios")
    @TeamOwnership(Team.INTEGRATION)
    PortfolioListResponse listPortfolios(@ClusterContext ClusterInfo clusterInfo);

    @POST
    @Path("consumermonitoring/all")
    @TeamOwnership(Team.INTEGRATION)
    PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo);

    @POST
    @Path("consumermonitoring/changed")
    @TeamOwnership(Team.INTEGRATION)
    PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo);
}
