package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;

@Path("/aggregation/controller/v1/system/process")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessService {
    @POST
    @Path("/statisticsandactivities/generate")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response generateStatisticsAndActivityAsynchronously(
            GenerateStatisticsAndActivitiesRequest request);

    @POST
    @Path("/transactions/update")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateTransactionsAsynchronously(UpdateTransactionsRequest request);
}
