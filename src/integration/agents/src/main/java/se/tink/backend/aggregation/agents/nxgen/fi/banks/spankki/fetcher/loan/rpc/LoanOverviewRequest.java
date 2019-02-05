package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewRequest extends SpankkiRequest {
    private String customerId;

    public LoanOverviewRequest setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }
}
