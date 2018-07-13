package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse extends AlandsBankenResponse {

    private LoanDetailsEntity loanDetailsVO;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetailsVO;
    }
}
