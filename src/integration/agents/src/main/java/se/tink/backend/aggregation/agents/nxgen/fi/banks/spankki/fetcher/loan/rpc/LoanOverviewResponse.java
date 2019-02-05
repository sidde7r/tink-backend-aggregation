package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities.LoanOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse extends SpankkiResponse {
    private List<LoanOverviewEntity> loanOverviews;

    public List<LoanOverviewEntity> getLoanOverviews() {
        return loanOverviews;
    }
}
