package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountHolderRequest;

@Path("/aggregation/controller/v1/account-holder")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface AccountHolderService {

    @POST
    @Path("/update")
    @TeamOwnership(Team.AGGREGATION_EXPERIENCE)
    AccountHolder updateAccountHolder(UpdateAccountHolderRequest request);
}
