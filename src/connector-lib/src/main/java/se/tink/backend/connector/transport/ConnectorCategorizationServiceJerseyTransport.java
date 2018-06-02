package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.CategorizationConnectorService;
import se.tink.backend.connector.api.ConnectorUserService;
import se.tink.backend.connector.controller.ConnectorCategorizationServiceController;
import se.tink.backend.connector.controller.ConnectorUserServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@Path("/connector/categorize")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllowClient("CATEGORIZATION")
@Api(value = "Categorization Service",
        description = "Categorizes transactions")
public class ConnectorCategorizationServiceJerseyTransport implements CategorizationConnectorService {
    private final ConnectorCategorizationServiceController controller;

    @Inject
    public ConnectorCategorizationServiceJerseyTransport(ConnectorCategorizationServiceController controller) {
        this.controller = controller;
    }

    @GET
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Categorize", notes = "Takes a transaction and returns categorized one.")
    public String categorize(
            @QueryParam(value = "market")
            @ApiParam(value = "Market code", required = true, example = "NL")
            @Valid
            @StringNotNullOrEmpty
                    String market,
            @QueryParam(value = "description")
            @ApiParam(value = "A merchant name if possible. If such value is not available, the description that is shown in the transaction list.", example = "Albert Heijn", required = true)
            @Valid
            @StringNotNullOrEmpty
                    String transactionDescription)
            throws RequestException {
        return controller.category(market, transactionDescription);
    }
}
