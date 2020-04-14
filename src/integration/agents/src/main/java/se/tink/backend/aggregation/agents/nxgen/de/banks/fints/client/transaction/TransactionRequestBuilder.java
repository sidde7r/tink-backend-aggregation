package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;

public interface TransactionRequestBuilder {
    FinTsRequest build(
            FinTsDialogContext dialogContext,
            FinTsAccountInformation account,
            String startingPoint);
}
