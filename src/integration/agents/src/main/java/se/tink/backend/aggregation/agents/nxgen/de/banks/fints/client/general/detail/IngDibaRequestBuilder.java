package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKIDNv2;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKVVBv3;

public class IngDibaRequestBuilder extends DefaultRequestBuilder {

    @Override
    public FinTsRequest getInitRequest(FinTsDialogContext dialogContext) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
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
        return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
    }
}
