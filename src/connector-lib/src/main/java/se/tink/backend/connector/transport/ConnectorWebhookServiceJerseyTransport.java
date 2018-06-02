package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.ConnectorWebhookService;
import se.tink.backend.connector.controller.ConnectorWebhookServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.connector.rpc.WebhookListEntity;
import se.tink.libraries.http.annotations.auth.AllowClient;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@Path("/connector/webhooks")
@Api(value = "Webhook Service", description = "Allows partners to set up webhooks for certain scopes.")
@AllowClient("CONNECTOR_CLIENT")
public class ConnectorWebhookServiceJerseyTransport implements ConnectorWebhookService {

    private final ConnectorWebhookServiceController controller;

    @Inject
    public ConnectorWebhookServiceJerseyTransport(ConnectorWebhookServiceController controller) {
        this.controller = controller;
    }

    @POST
    @ApiOperation(value = "Set up a webhook.", notes = "Set up a new webhook for all users, giving the possibility to get pushed updates for certain events. The webhook will automatically concern both old and new users.")
    @TeamOwnership(Team.DATA)
    @Override
    public void createWebhook(
            @ApiParam(value = "The webhook request.", required = true) @Valid WebhookEntity webhookEntity)
            throws RequestException {
        controller.createGlobalWebhook(webhookEntity);
    }

    @GET
    @ApiOperation(value = "Get all registered webhooks.", notes = "Returns an object with a list of all the registered webhooks.")
    @TeamOwnership(Team.DATA)
    @Override
    public WebhookListEntity getWebhooks() {
        return new WebhookListEntity(controller.getWebhooks());
    }

    @DELETE
    @Path("/{id}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete a webhook.", notes = "")
    @Override
    public void deleteWebhook(@PathParam("id") @ApiParam(value = "Internal Tink ID for the webhook.", required = true,
            example = "2ce1f090a9304f13a15458d480f8a85d") @StringNotNullOrEmpty String id) throws RequestException {
        controller.deleteWebhook(id);
    }

}
