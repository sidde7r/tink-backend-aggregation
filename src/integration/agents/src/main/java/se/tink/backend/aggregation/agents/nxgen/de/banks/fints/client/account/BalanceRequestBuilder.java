package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv5;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv7;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.SupportedRequestSegments;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public interface BalanceRequestBuilder {
    static BalanceRequestBuilder getRequestBuilder(FinTsDialogContext dialogContext) {
        OptionalInt version =
                SupportedRequestSegments.getHighestCommonVersion(dialogContext, SegmentType.HKSAL);

        if (!version.isPresent()) {
            throw new IllegalArgumentException(
                    "We weren't able to find a commonly supported version of HKSAL");
        }

        switch (version.getAsInt()) {
            case 5:
                return HKSAL_5;
            case 6:
                return HKSAL_6;
            case 7:
                return HKSAL_7;
            default:
                throw new IllegalArgumentException(
                        String.format(
                                "This version of HKSAL segment: %d is not supported",
                                version.getAsInt()));
        }
    }

    FinTsRequest build(FinTsDialogContext context, HIUPD account);

    BalanceRequestBuilder HKSAL_5 =
            (dialogContext, account) -> {
                List<BaseRequestPart> additionalSegments = new ArrayList<>();
                additionalSegments.add(
                        HKSALv5.builder()
                                .blz(dialogContext.getConfiguration().getBlz())
                                .accountNumber(account.getAccountNumber())
                                .subAccountNumber(account.getSubAccountNumber())
                                .build());
                if (dialogContext.doesOperationRequireTAN(SegmentType.HKSAL)) {
                    additionalSegments.add(
                            HKTANv6.builder()
                                    .tanProcess("4")
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
    BalanceRequestBuilder HKSAL_6 =
            (dialogContext, account) -> {
                List<BaseRequestPart> additionalSegments = new ArrayList<>();
                additionalSegments.add(
                        HKSALv6.builder()
                                .blz(dialogContext.getConfiguration().getBlz())
                                .accountNumber(account.getAccountNumber())
                                .subAccountNumber(account.getSubAccountNumber())
                                .build());
                if (dialogContext.doesOperationRequireTAN(SegmentType.HKSAL)) {
                    additionalSegments.add(
                            HKTANv6.builder()
                                    .tanProcess("4")
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
    BalanceRequestBuilder HKSAL_7 =
            (dialogContext, account) -> {
                List<BaseRequestPart> additionalSegments = new ArrayList<>();
                additionalSegments.add(
                        HKSALv7.builder()
                                .blz(dialogContext.getConfiguration().getBlz())
                                .accountNumber(account.getAccountNumber())
                                .subAccountNumber(account.getSubAccountNumber())
                                .build());
                if (dialogContext.doesOperationRequireTAN(SegmentType.HKSAL)) {
                    additionalSegments.add(
                            HKTANv6.builder()
                                    .tanProcess("4")
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
}
