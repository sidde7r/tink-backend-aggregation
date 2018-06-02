package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.ConnectorTransactionService;
import se.tink.backend.connector.controller.ConnectorTransactionServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.DeleteTransactionAccountsContainer;
import se.tink.backend.connector.rpc.UpdateTransactionAccountContainer;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@Path("/connector/users/{externalUserId}/transactions")
@Api(value = "Transaction Service",
        description = "The connector is the component that allows financial institutions to push data to Tink.")
@AllowClient("CONNECTOR_CLIENT")
public class ConnectorTransactionServiceJerseyTransport implements ConnectorTransactionService {

    private final ConnectorTransactionServiceController controller;

    @Inject
    public ConnectorTransactionServiceJerseyTransport(ConnectorTransactionServiceController controller) {
        this.controller = controller;
    }

    @POST
    @ApiOperation(value = "Ingest transactions",
            notes = "Takes historical or real time transactions together with an account.")
    @TeamOwnership(Team.DATA)
    @Override
    public void ingestTransactions(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
            @StringNotNullOrEmpty String externalUserId,
            @ApiParam(value = "Container of account and transactions.", required = true)
            @Valid CreateTransactionAccountContainer entity) throws RequestException {

        controller.ingestTransactions(externalUserId, entity);
    }

    @PUT
    @Path("/{externalTransactionId}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Update transactions", notes = "Updates a single transaction related to an account.")
    @Override
    public void updateTransactions(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
            @StringNotNullOrEmpty String externalUserId,
            @PathParam("externalTransactionId")
            @ApiParam(value = "Persistent identifier for the transaction.", required = true,
                    example = "40dc04e5353547378c84f34ffc88f853")
            @StringNotNullOrEmpty String externalTransactionId,
            @ApiParam(value = "Container of account and transactions", required = true)
            @Valid UpdateTransactionAccountContainer entity) throws RequestException {
        controller.updateTransactions(externalUserId, externalTransactionId, entity);
    }

    @POST
    @Path("delete")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete transactions",
            notes = "Removes transactions. When deleting transactions, it's only the externalId of each transaction that is necessary.")
    @Override
    public void deleteTransactions(
            @PathParam("externalUserId")
            @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d")
            @StringNotNullOrEmpty String externalUserId,
            @ApiParam(value = "Container of account and transactions.", required = true)
            @Valid DeleteTransactionAccountsContainer entity) throws RequestException {

        controller.deleteTransactions(externalUserId, entity);
    }
}
