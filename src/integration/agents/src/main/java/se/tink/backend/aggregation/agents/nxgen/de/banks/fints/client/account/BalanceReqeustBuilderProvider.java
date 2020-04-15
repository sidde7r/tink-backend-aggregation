package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv5;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSALv7;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.SupportedRequestSegments;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

@NoArgsConstructor(access = PRIVATE)
public class BalanceReqeustBuilderProvider {

    public static BalanceRequestBuilder getRequestBuilder(FinTsDialogContext dialogContext) {
        OptionalInt version =
                SupportedRequestSegments.getHighestCommonVersion(dialogContext, SegmentType.HKSAL);

        if (!version.isPresent()) {
            throw new IllegalArgumentException(
                    "We weren't able to find a commonly supported version of HKSAL");
        }

        switch (version.getAsInt()) {
            case 5:
                return HKSAL_V5;
            case 6:
                return HKSAL_V6;
            case 7:
                return HKSAL_V7;
            default:
                throw new IllegalArgumentException(
                        String.format(
                                "This version of HKSAL segment: %d is not supported",
                                version.getAsInt()));
        }
    }

    private static final BalanceRequestBuilder HKSAL_V5 =
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
                                    .tanProcessVariant(
                                            HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
    private static final BalanceRequestBuilder HKSAL_V6 =
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
                                    .tanProcessVariant(
                                            HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
    private static final BalanceRequestBuilder HKSAL_V7 =
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
                                    .tanProcessVariant(
                                            HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                                    .segmentType(SegmentType.HKSAL)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }
                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
}
