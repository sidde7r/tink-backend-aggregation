package se.tink.backend.connector.transport;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.api.ConnectorBatchService;
import se.tink.backend.connector.controller.ConnectorTransactionServiceController;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.response.RequestErrorResponseMapper;
import se.tink.backend.connector.rpc.CreateTransactionBatch;
import se.tink.backend.connector.rpc.IngestTransactionEntity;
import se.tink.backend.connector.rpc.IngestTransactionStatus;
import se.tink.backend.connector.rpc.TransactionBatchResponse;
import se.tink.libraries.http.annotations.auth.AllowClient;

@Path("/connector/batch")
@Api(value = "Batch Service",
        description = "The connector is the component that allows financial institutions to push data to Tink.")
@AllowClient("CONNECTOR_CLIENT")
public class ConnectorBatchServiceJerseyTransport implements ConnectorBatchService {

    private final ConnectorTransactionServiceController controller;

    @Inject
    public ConnectorBatchServiceJerseyTransport(ConnectorTransactionServiceController controller) {
        this.controller = controller;
    }

    @POST
    @ApiOperation(value = "Ingest transactions",
            notes = "Takes historical or real time transactions together with an account.")
    @TeamOwnership(Team.DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public TransactionBatchResponse ingestTransactionsBatch(
            @ApiParam(value = "Batch of Ingest Transaction entities.", required = true)
            @Valid CreateTransactionBatch batch) throws RequestException {

        TransactionBatchResponse response = new TransactionBatchResponse();
        RequestErrorResponseMapper responseMapper = new RequestErrorResponseMapper();

        for (IngestTransactionEntity entity : batch.getIngestEntities()) {
            try {
                controller.ingestTransactions(entity.getExternalUserId(), entity.getContainer());
            } catch (RequestException exception) {
                Response singleResponse = responseMapper.toResponse(exception);
                response.addStatus(IngestTransactionStatus.create(
                        entity.getEntityId(),
                        singleResponse.getStatus(),
                        singleResponse.getEntity()));
            }
        }

        return response;
    }
}
