package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.TransactionsRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class TransactionsRequest {
    private final TransactionsRequestPayload payload;
}
