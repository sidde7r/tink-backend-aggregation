package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.ConnectorUserService;
import se.tink.backend.connector.controller.ConnectorUserServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@Path("/connector/users")
@Api(value = "User Service",
        description = "The connector is the component that allows financial institutions to push data to Tink.")
@AllowClient("CONNECTOR_CLIENT")
public class ConnectorUserServiceJerseyTransport implements ConnectorUserService {

    private final ConnectorUserServiceController controller;

    @Inject
    public ConnectorUserServiceJerseyTransport(ConnectorUserServiceController controller) {
        this.controller = controller;
    }

    @POST
    @ApiOperation(value = "Activate a user", notes = "Activates a user.")
    @TeamOwnership(Team.DATA)
    public void createUser(@ApiParam(value = "The user", required = true) @Valid UserEntity userEntity)
            throws RequestException {

        controller.createUser(userEntity);
    }

    @PUT
    @Path("{externalUserId}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Update a user", notes = "Updates a user.")
    public void updateUser(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
            @StringNotNullOrEmpty String externalUserId,
            @ApiParam(value = "The user", required = true) @Valid UserEntity entity) throws RequestException {

        controller.updateUser(externalUserId, entity);
    }

    @DELETE
    @Path("{externalUserId}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete a user", notes = "Deletes a user.")
    public void deleteUser(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
            @StringNotNullOrEmpty String externalUserId) throws RequestException {

        controller.deleteUser(externalUserId);
    }
}
