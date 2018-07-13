package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.google.common.collect.Range;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.core.transfer.SignableOperationStatuses;

public class SwedbankTransferResponseFilter extends ClientFilter {
    protected AgentContext context;

    public SwedbankTransferResponseFilter(AgentContext context) {
        this.context = context;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        ClientResponse response = getNext().handle(clientRequest);

        if (!is2xxStatus(response)) {
            throwIfCreateRecipientException(response);
        }

        return response;
    }

    private boolean is2xxStatus(ClientResponse clientResponse) {
        return Range.closed(200, 299).contains(clientResponse.getStatus());
    }

    private void throwIfCreateRecipientException(ClientResponse createRecipientResponse) {
        ErrorResponse errorResponse = createRecipientResponse.getEntity(ErrorResponse.class);
        if (errorResponse.getErrorMessages().getGeneral().size() == 0) {
            return;
        }

        String errorCode = errorResponse.getErrorMessages().getGeneral().get(0).getCode();
        if (!"STRONGER_AUTHENTICATION_NEEDED".equalsIgnoreCase(errorCode)) {
            return;
        }

        String userMessage = context.getCatalog().getString(
                "In order to add new recipients you need to activate Mobile BankID for extended use. This "
                        + "is done in the Internet bank on the page BankID (found in the tab Tillval).");

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(userMessage).build();
    }
}
