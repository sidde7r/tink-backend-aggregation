package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.dialog.detail;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;

public interface DialogRequestBuilder {
    FinTsRequest getInitRequest(FinTsDialogContext dialogContext);

    FinTsRequest getInitializeSessionRequest(FinTsDialogContext dialogContext);

    FinTsRequest getTanMedium(FinTsDialogContext dialogContext);

    FinTsRequest getFinishRequest(FinTsDialogContext dialogContext);
}
