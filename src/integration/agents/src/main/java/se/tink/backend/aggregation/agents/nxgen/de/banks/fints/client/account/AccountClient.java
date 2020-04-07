package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSPAv1;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

@AllArgsConstructor
public class AccountClient {

    private final FinTsRequestProcessor requestProcessor;
    private final FinTsDialogContext dialogContext;

    public FinTsResponse getSepaDetailsForAllAccounts() {
        FinTsRequest request = getSepaDetailsRequest();
        FinTsResponse response = requestProcessor.process(request);

        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException(
                    "Couldn't fetch SEPA details for user's accounts");
        }

        return response;
    }

    public FinTsResponse getBalanceForAccount(
            BalanceRequestBuilder requestBuilder, HIUPD hiupdAccount) {
        FinTsRequest request = requestBuilder.build(dialogContext, hiupdAccount);
        FinTsResponse response = requestProcessor.process(request);

        if (!response.isSuccess()) {
            throw new UnsuccessfulApiCallException(
                    String.format(
                            "Couldn't fetch balance for AccountNumber: %s , AccountType: %s",
                            hiupdAccount.getAccountNumber(), hiupdAccount.getAccountType()));
        }

        return response;
    }

    private FinTsRequest getSepaDetailsRequest() {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
        additionalSegments.add(new HKSPAv1());
        if (requireTAN(SegmentType.HKSPA)) {
            additionalSegments.add(
                    HKTANv6.builder()
                            .tanProcess("4")
                            .segmentType(SegmentType.HKSPA)
                            .tanMediumName(dialogContext.getChosenTanMedium())
                            .build());
        }
        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }

    private boolean requireTAN(SegmentType segmentType) {
        return dialogContext.doesOperationRequireTAN(segmentType);
    }
}
