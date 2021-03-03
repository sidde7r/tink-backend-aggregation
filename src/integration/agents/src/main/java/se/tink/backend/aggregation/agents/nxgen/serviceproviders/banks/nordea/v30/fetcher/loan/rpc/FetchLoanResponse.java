package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchLoanResponse {
    private List<LoansEntity> loans;
}
