package se.tink.backend.system.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.system.rpc.SendNotificationsRequest;

@Path("/notificationgateway")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface NotificationGatewayService {
    @POST
    @Path("/send")
    @TeamOwnership(Team.PFM)
    void sendNotificationsAsynchronously(SendNotificationsRequest request);

    @POST
    @Path("/send/wait")
    @TeamOwnership(Team.PFM)
    void sendNotificationsSynchronously(SendNotificationsRequest request);

}
