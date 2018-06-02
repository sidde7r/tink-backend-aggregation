package se.tink.backend.connector.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.rpc.seb.AccountListEntity;
import se.tink.backend.connector.rpc.seb.DeleteTransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.RollbackRequest;
import se.tink.backend.connector.rpc.seb.TransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.UserEntity;
import se.tink.backend.core.Notification;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;
import se.tink.libraries.http.annotations.auth.AllowClient;

@Path("/connector/seb")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Connector Service",
        description = "The connector is the component that allows financial institutions to push data to Tink.")
@AllowClient("SEB")
public interface SEBConnectorService {

    @POST
    @Path("user")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Activate a user", notes = "Activates a user.")
    void user(@ApiParam(value="The user", required = true) UserEntity entity);

    @DELETE
    @Path("user/{externalUserId}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete a user", notes = "Deletes a user.")
    void deleteUser(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
                    String externalUserId);

    @POST
    @Path("user/{externalUserId}/accounts")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Ingest accounts", notes = "Takes a list of accounts and the corresponding user ID.")
    void accounts(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
                    String externalUserId,
            @ApiParam(value = "The accounts.", required = true)
                    AccountListEntity entity);

    @POST
    @Path("user/{externalUserId}/transactions")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Ingest transactions",
            notes = "Takes historical or real time transactions together with an account.")
    void transactions(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
                    String externalUserId,
            @ApiParam(value = "Container of account and transactions.", required = true)
                    TransactionAccountContainer container);

    @POST
    @Path("user/{externalUserId}/transactions/delete")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete transactions",
            notes = "Removes transactions. When deleting transactions, it's only the externalId of each transaction that is necessary.")
    void deleteTransactions(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
                    String externalUserId,
            @ApiParam(value = "Container of account and transactions.", required = true)
                    DeleteTransactionAccountContainer container);

    @POST
    @Path("user/{externalUserId}/notifications")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Send notification", notes = "Sends a notification to a user.")
    void sendNotification(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
                    String externalUserId,
            @ApiParam(value = "Notification", required = true)
                    Notification notification);

    @GET
    @Path("ping")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();

    @POST
    @Path("rollback")
    @TeamOwnership(Team.DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Initiate transaction rollback",
            notes = "Initiates a rollback of transactions with the given checkpoint IDs"
    )
    void rollback(@ApiParam(value = "Object with list of checkpoint IDs", required = true) RollbackRequest rollbackRequest);

}
