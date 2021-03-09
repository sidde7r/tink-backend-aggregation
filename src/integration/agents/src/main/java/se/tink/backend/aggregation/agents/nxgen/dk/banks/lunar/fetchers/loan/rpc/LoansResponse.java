package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansResponse {
    private List<LoanEntity> loans;

    public List<LoanEntity> getLoans() {
        return ListUtils.emptyIfNull(loans);
    }
}
