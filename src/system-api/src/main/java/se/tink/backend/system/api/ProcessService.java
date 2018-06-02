package se.tink.backend.system.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.rpc.ReplayQueueRequest;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;

@Path("/process")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
public interface ProcessService {
    @POST
    @Path("/transactions/update")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateTransactionsAsynchronously(UpdateTransactionsRequest request);

    @POST
    @Path("/transactions/update/wait")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response updateTransactionsSynchronously(UpdateTransactionsRequest request);

    @POST
    @Path("/statisticsandactivities/generate")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request);

    @POST
    @Path("/statisticsandactivities/generate/wait")
    @TeamOwnership(Team.DATA)
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    Response generateStatisticsAndActivitySynchronous(GenerateStatisticsAndActivitiesRequest request);

    @POST
    @Path("/queue/reset")
    @TeamOwnership(Team.DATA)
    Response resetConnectorQueues(ReplayQueueRequest request);

    @POST
    @Path("/{id}/statisticsandactivities/generate")
    @TeamOwnership(Team.DATA)
    void generateStatisticsAndActivitiesWithoutNotifications(@PathParam("id") String userId, StatisticMode mode);

    @POST
    @Path("/{id}/properties/generate")
    @TeamOwnership(Team.DATA)
    void generateProperties(@PathParam("id") String userId);
}
