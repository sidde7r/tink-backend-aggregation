package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanResponse {
    @JsonProperty private List<LoansEntity> loans;

    public List<LoansEntity> getLoans() {
        return loans;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setLoans(List<LoansEntity> loans) {
        this.loans = loans;
    }
}
