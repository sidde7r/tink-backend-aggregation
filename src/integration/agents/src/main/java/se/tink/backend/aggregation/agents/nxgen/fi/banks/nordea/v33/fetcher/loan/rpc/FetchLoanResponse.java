package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanResponse {
    private List<LoansEntity> loans;

    public List<LoansEntity> getLoans() {
        return ListUtils.emptyIfNull(loans);
    }
}
