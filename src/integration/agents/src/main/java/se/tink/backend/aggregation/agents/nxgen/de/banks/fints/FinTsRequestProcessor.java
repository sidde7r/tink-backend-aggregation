package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.TAN_GENERATED_SUCCESSFULLY;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HITAN;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HNHBK;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;

public class FinTsRequestProcessor {
    private FinTsDialogContext dialogContext;
    private final FinTsRequestSender requestSender;
    private final TanAnswerProvider tanAnswerProvider;

    public FinTsRequestProcessor(
            FinTsDialogContext dialogContext,
            FinTsRequestSender requestSender,
            TanAnswerProvider tanAnswerProvider) {
        this.dialogContext = dialogContext;
        this.requestSender = requestSender;
        this.tanAnswerProvider = tanAnswerProvider;
    }

    /** Transfers request to the bank and handles TAN if needed */
    public FinTsResponse process(FinTsRequest request) {
        FinTsResponse response = sendRequest(request);
        if (hasTANBeenGenerated(response)) {
            response = processTAN(response);
        }
        return response;
    }

    private FinTsResponse processTAN(FinTsResponse responseWithChallenge) {
        HITAN hitan = responseWithChallenge.findSegmentThrowable(HITAN.class);

        String taskReference = hitan.getTaskReference();
        String tanAnswer = tanAnswerProvider.getTanAnswer();
        updateTanContext(taskReference, tanAnswer);

        FinTsRequest request = challengeSolvedRequest();
        FinTsResponse response = sendRequest(request);
        return response;
    }

    private FinTsResponse sendRequest(FinTsRequest request) {
        FinTsResponse response = requestSender.sendRequest(request);

        dialogContext.setMessageNumber(dialogContext.getMessageNumber() + 1);
        dialogContext.generateNewSecurityReference();

        if ("0".equals(dialogContext.getDialogId())) {
            response.findSegment(HNHBK.class)
                    .ifPresent(hnhbk -> dialogContext.setDialogId(hnhbk.getDialogId()));
        }
        return response;
    }

    private void updateTanContext(String taskReference, String tanAnswer) {
        dialogContext.setTaskReference(taskReference);
        dialogContext.setTanAnswer(tanAnswer);
    }

    private FinTsRequest challengeSolvedRequest() {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
        additionalSegments.add(
                HKTANv6.builder()
                        .tanProcess("2")
                        .taskReference(dialogContext.getTaskReference())
                        .furtherTanFollows(false)
                        .build());
        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }

    private boolean hasTANBeenGenerated(FinTsResponse response) {
        return response.hasStatusCodeOf(TAN_GENERATED_SUCCESSFULLY);
    }
}
