package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanResponse {
    @JsonProperty
    private List<LoansEntity> loans;

    public Stream<LoansEntity> stream() {
        return loans.stream();
    }
}
