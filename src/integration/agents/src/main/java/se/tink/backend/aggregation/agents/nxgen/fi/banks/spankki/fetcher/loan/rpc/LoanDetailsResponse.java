package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@JsonObject
public class LoanDetailsResponse extends SpankkiResponse {
    private LoanDetailsEntity loan;

    public LoanDetailsEntity getLoan() {
        return loan;
    }

    public LoanAccount toTinkLoanAccount() {
        return loan.toTinkLoanAccount();
    }
}
