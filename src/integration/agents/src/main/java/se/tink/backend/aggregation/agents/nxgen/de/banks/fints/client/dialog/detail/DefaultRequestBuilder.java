package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKENDv1;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKIDNv2;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKSYNv3;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTABv4;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKVVBv3;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public class DefaultRequestBuilder implements DialogRequestBuilder {
    @Override
    public FinTsRequest getInitRequest(FinTsDialogContext dialogContext) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>(3);
        additionalSegments.add(
                HKIDNv2.builder()
                        .systemId(dialogContext.getSystemId())
                        .blz(dialogContext.getConfiguration().getBlz())
                        .username(dialogContext.getConfiguration().getUsername())
                        .build());
        additionalSegments.add(
                HKVVBv3.builder()
                        .productId(dialogContext.getSecretsConfiguration().getProductId())
                        .productVersion(dialogContext.getSecretsConfiguration().getProductVersion())
                        .build());
        additionalSegments.add(
                HKTANv6.builder()
                        .tanProcessVariant(HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                        .segmentType(SegmentType.HKIDN)
                        .tanMediumName(dialogContext.getChosenTanMedium())
                        .build());
        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }

    @Override
    public FinTsRequest getInitializeSessionRequest(FinTsDialogContext dialogContext) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>(3);
        additionalSegments.add(
                HKIDNv2.builder()
                        .systemId(dialogContext.getSystemId())
                        .blz(dialogContext.getConfiguration().getBlz())
                        .username(dialogContext.getConfiguration().getUsername())
                        .build());
        additionalSegments.add(
                HKVVBv3.builder()
                        .productId(dialogContext.getSecretsConfiguration().getProductId())
                        .productVersion(dialogContext.getSecretsConfiguration().getProductVersion())
                        .build());
        additionalSegments.add(new HKSYNv3());

        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }

    @Override
    public FinTsRequest getTanMedium(FinTsDialogContext dialogContext) {
        BaseRequestPart hktab = new HKTABv4();
        return FinTsRequest.createEncryptedRequest(dialogContext, Collections.singletonList(hktab));
    }

    @Override
    public FinTsRequest getFinishRequest(FinTsDialogContext dialogContext) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
        additionalSegments.add(HKENDv1.builder().dialogId(dialogContext.getDialogId()).build());
        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }
}
