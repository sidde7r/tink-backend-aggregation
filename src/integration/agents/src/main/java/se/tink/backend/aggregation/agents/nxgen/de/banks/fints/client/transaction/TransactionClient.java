package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.MORE_INFORMATION_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusMessage.NO_ACTIVE_PHONE_NUMBER_WARNING;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIRMS;

public class TransactionClient {

    private final FinTsRequestProcessor requestProcessor;
    private final FinTsDialogContext dialogContext;

    public TransactionClient(
            FinTsRequestProcessor requestProcessor, FinTsDialogContext dialogContext) {
        this.requestProcessor = requestProcessor;
        this.dialogContext = dialogContext;
    }

    public List<FinTsResponse> getTransactionResponses(
            TransactionRequestBuilder requestBuilder, FinTsAccountInformation account) {
        List<FinTsResponse> responses = new ArrayList<>();
        getTransactionsResponsesRecur(requestBuilder, account, null, responses);
        return responses;
    }

    private void getTransactionsResponsesRecur(
            TransactionRequestBuilder requestBuilder,
            FinTsAccountInformation account,
            String startingPoint,
            List<FinTsResponse> mutableListOfResponses) {
        FinTsRequest request = requestBuilder.build(dialogContext, account, startingPoint);
        FinTsResponse response = requestProcessor.process(request);

        if (response.hasStatusCodeOf(FinTsConstants.StatusCode.NO_ENTRY)
                && !response.hasStatusMessageOf(NO_ACTIVE_PHONE_NUMBER_WARNING)) {
            return;
        }

        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Fetching transaction failed");
        }

        mutableListOfResponses.add(response);
        Optional<String> maybeStartingPoint = extractStartingPoint(response);
        maybeStartingPoint.ifPresent(
                s ->
                        getTransactionsResponsesRecur(
                                requestBuilder, account, s, mutableListOfResponses));
    }

    private Optional<String> extractStartingPoint(FinTsResponse response) {
        return response.findSegments(HIRMS.class).stream()
                .flatMap(x -> x.getResponsesWithCode(MORE_INFORMATION_AVAILABLE).stream())
                .findFirst()
                .map(x -> x.getParameters().get(0));
    }
}
