package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.MORE_INFORMATION_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusMessage.NO_ACTIVE_PHONE_NUMBER_WARNING;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIRMS;

@AllArgsConstructor
public class TransactionClient {

    private final FinTsRequestProcessor requestProcessor;
    private final FinTsDialogContext dialogContext;

    public List<FinTsResponse> getTransactionResponses(
            TransactionRequestBuilder requestBuilder, FinTsAccountInformation account) {
        return getTransactionsResponsesRecurList(requestBuilder, account, null);
    }

    private LinkedList<FinTsResponse> getTransactionsResponsesRecurList(
            TransactionRequestBuilder requestBuilder,
            FinTsAccountInformation account,
            String startingPoint) {
        FinTsRequest request = requestBuilder.build(dialogContext, account, startingPoint);
        FinTsResponse response = requestProcessor.process(request);

        if (response.hasStatusCodeOf(FinTsConstants.StatusCode.NO_ENTRY)
                && !response.hasStatusMessageOf(NO_ACTIVE_PHONE_NUMBER_WARNING)) {
            return new LinkedList<>();
        }

        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException("Fetching transaction failed");
        }

        Optional<String> maybeStartingPoint = extractStartingPoint(response);
        LinkedList<FinTsResponse> responses =
                maybeStartingPoint
                        .map(
                                point ->
                                        getTransactionsResponsesRecurList(
                                                requestBuilder, account, point))
                        .orElse(new LinkedList<>());
        responses.addFirst(response);
        return responses;
    }

    private Optional<String> extractStartingPoint(FinTsResponse response) {
        return response.findSegments(HIRMS.class).stream()
                .flatMap(x -> x.getResponsesWithCode(MORE_INFORMATION_AVAILABLE).stream())
                .findFirst()
                .map(x -> x.getParameters().get(0));
    }
}
