package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansResponse {
    @JsonProperty("loans")
    private List<LoanEntity> loans;

    public List<LoanEntity> getLoans() {
        return Optional.ofNullable(loans).orElse(Collections.emptyList());
    }
}
