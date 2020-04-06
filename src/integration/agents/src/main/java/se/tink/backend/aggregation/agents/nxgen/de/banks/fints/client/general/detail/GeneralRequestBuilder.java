package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.general.detail;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;

public interface GeneralRequestBuilder {
    FinTsRequest getInitRequest(FinTsDialogContext dialogContext);

    FinTsRequest getInitializeSessionRequest(FinTsDialogContext dialogContext);

    FinTsRequest getTanMedium(FinTsDialogContext dialogContext);

    FinTsRequest getFinishRequest(FinTsDialogContext dialogContext);
}
