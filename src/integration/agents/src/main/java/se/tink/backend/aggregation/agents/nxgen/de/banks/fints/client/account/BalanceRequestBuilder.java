package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

public interface BalanceRequestBuilder {
    FinTsRequest build(FinTsDialogContext dialogContext, HIUPD account);
}
