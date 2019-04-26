package se.tink.backend.aggregation.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;

@Path("/creditsafe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CreditSafeService {
    @POST
    @Path("consumermonitoring/delete")
    @TeamOwnership(Team.AGGREGATION)
    void removeConsumerMonitoring(
            RemoveMonitoredConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("consumermonitoring")
    @TeamOwnership(Team.AGGREGATION)
    Response addConsumerMonitoring(
            AddMonitoredConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo);

    @GET
    @Path("consumermonitoring/portfolios")
    @TeamOwnership(Team.AGGREGATION)
    PortfolioListResponse listPortfolios(@ClientContext ClientInfo clientInfo);

    @POST
    @Path("consumermonitoring/all")
    @TeamOwnership(Team.AGGREGATION)
    PageableConsumerCreditSafeResponse listMonitoredConsumers(
            PageableConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo);

    @POST
    @Path("consumermonitoring/changed")
    @TeamOwnership(Team.AGGREGATION)
    PageableConsumerCreditSafeResponse listChangedConsumers(
            ChangedConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo);
}
