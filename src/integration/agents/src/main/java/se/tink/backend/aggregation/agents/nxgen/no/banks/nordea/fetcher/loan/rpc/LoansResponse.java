package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoansResponse {
    private List<LoanEntity> loans;
}
