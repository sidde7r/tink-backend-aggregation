package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.TransactionsResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsResponse {
    private TransactionsResponsePayload payload;
}
