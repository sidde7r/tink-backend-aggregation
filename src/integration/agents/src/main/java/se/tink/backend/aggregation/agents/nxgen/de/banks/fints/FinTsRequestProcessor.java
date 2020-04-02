package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.TAN_GENERATED_SUCCESSFULLY;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.TanContextV6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.Header;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.TanContext;
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

    public FinTsResponse process(FinTsRequest request) {
        FinTsResponse response = sendRequest(request);
        if (hasTANBeenGenerated(response)) {
            response = processTAN(response);
        }
        return response;
    }

    private FinTsResponse processTAN(FinTsResponse responseWithChallenge) {
        TanContext tanContext = responseWithChallenge.findSegmentThrowable(TanContext.class);

        String taskReference = tanContext.getTaskReference();
        String tanAnswer = tanAnswerProvider.getTanAnswer();
        updateTanContext(taskReference, tanAnswer);

        FinTsRequest request = challengeSolvedRequest();
        return sendRequest(request);
    }

    private FinTsResponse sendRequest(FinTsRequest request) {
        FinTsResponse response = requestSender.sendRequest(request);

        dialogContext.setMessageNumber(dialogContext.getMessageNumber() + 1);
        dialogContext.generateNewSecurityReference();

        if (dialogContext.isDialogIdUninitialized()) {
            response.findSegment(Header.class)
                    .ifPresent(header -> dialogContext.setDialogId(header.getDialogId()));
        }
        return response;
    }

    private void updateTanContext(String taskReference, String tanAnswer) {
        dialogContext.setTaskReference(taskReference);
        dialogContext.setTanAnswer(tanAnswer);
    }

    private FinTsRequest challengeSolvedRequest() {
        List<BaseRequestPart> additionalSegments =
                Collections.singletonList(
                        TanContextV6.builder()
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
