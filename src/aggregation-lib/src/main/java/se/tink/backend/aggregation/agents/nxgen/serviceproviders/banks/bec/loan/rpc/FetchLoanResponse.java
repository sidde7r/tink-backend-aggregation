package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanResponse {
    private List<MortgageLoanEntity> mortgageLoanList;

    public List<MortgageLoanEntity> getMortgageLoanList() {
        return mortgageLoanList;
    }
}
