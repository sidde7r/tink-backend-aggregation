package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsAccountEntity extends AccountEntity {
    private String interest;

    public String getInterest() {
        return interest;
    }
}
