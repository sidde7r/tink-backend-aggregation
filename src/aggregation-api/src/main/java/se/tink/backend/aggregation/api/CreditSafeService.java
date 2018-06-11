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
import se.tink.backend.aggregation.rpc.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.ChangedConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeResponse;
import se.tink.backend.aggregation.rpc.PortfolioListResponse;
import se.tink.backend.aggregation.rpc.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.SeedPersonDataRequest;
import se.tink.backend.aggregation.rpc.SeedPersonDataResponse;

@Path("/creditsafe")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
@Deprecated
public interface CreditSafeService {
    @DELETE
    @Path("consumermonitoring")
    @TeamOwnership(Team.PFM)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    public void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request);

    @POST
    @Path("consumermonitoring")
    @TeamOwnership(Team.PFM)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request);

    @GET
    @Path("consumermonitoring/portfolios")
    @TeamOwnership(Team.PFM)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    public PortfolioListResponse listPortfolios();

    @POST
    @Path("consumermonitoring/all")
    @TeamOwnership(Team.PFM)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request);

    @POST
    @Path("consumermonitoring/changed")
    @TeamOwnership(Team.PFM)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request);
    
    @POST
    @Path("seedPersonData")
    @TeamOwnership(Team.PFM)
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public SeedPersonDataResponse seedPersonData(SeedPersonDataRequest request);
}
