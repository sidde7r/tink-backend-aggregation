package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewRequest extends SpankkiRequest {
    private String customerId;

    public LoanOverviewRequest setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }
}
