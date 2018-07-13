package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AllFundsRequest extends SpankkiRequest {
    private String categoryId = "";
}
