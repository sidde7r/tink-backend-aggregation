package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanOverviewResponse {
    private String totalRemainingDebt;
    private List<LoansEntity> loans;

    public String getTotalRemainingDebt() {
        return totalRemainingDebt;
    }

    public List<LoansEntity> getLoans() {
        return Optional.ofNullable(loans).orElse(Lists.newArrayList());
    }
}
