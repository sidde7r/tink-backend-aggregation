package se.tink.backend.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.UpdateTransactionsRequest;

@Path("/process")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProcessService {
    @POST
    @Path("/statisticsandactivities/generate")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request);

    @POST
    @Path("/transactions/update")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateTransactionsAsynchronously(UpdateTransactionsRequest request);
}
