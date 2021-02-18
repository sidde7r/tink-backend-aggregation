package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanListResponse {
    private List<LoanEntity> loans;

    public List<LoanEntity> getLoans() {
        return loans == null ? Collections.emptyList() : loans;
    }
}
