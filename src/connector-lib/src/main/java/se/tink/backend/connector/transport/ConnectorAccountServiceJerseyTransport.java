package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.ConnectorAccountService;
import se.tink.backend.connector.controller.ConnectorAccountServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.AccountListEntity;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@Path("connector/users/{externalUserId}/accounts")
@Api(value = "Account Service",
        description = "The connector is the component that allows financial institutions to push data to Tink.")
@AllowClient("CONNECTOR_CLIENT")
public class ConnectorAccountServiceJerseyTransport implements ConnectorAccountService {

    private ConnectorAccountServiceController controller;

    @Inject
    public ConnectorAccountServiceJerseyTransport(ConnectorAccountServiceController controller) {
        this.controller = controller;
    }

    @POST
    @ApiOperation(value = "Ingest accounts", notes = "Takes a list of accounts and the corresponding user ID.")
    @TeamOwnership(Team.DATA)
    @Override
    public void createAccounts(
            @PathParam("externalUserId") @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d") @StringNotNullOrEmpty String externalUserId,
            @ApiParam(value = "The accounts.", required = true) @Valid AccountListEntity entity)
            throws RequestException {

        controller.createAccounts(externalUserId, entity);
    }

    @DELETE
    @Path("/{externalAccountId}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete account", notes = "Deletes the account with the given account ID.")
    @Override
    public void deleteAccount(
            @PathParam("externalUserId") @ApiParam(value = "Persistent identifier for the user.", required = true,
                    example = "2ce1f090a9304f13a15458d480f8a85d") @StringNotNullOrEmpty String externalUserId,
            @PathParam("externalAccountId") @ApiParam(value = "Persistent identifier for the account.", required = true,
                    example = "2d3bd65493b549e1927d97a2d0683ab9") @StringNotNullOrEmpty String externalAccountId)
            throws RequestException {

        controller.deleteAccount(externalUserId, externalAccountId);
    }
}
