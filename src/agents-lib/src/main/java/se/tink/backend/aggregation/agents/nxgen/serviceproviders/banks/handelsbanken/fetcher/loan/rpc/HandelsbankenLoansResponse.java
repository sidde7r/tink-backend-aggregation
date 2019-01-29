package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.rpc;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.agents.rpc.Credentials;

public abstract class HandelsbankenLoansResponse extends BaseResponse {
    public abstract Collection<LoanAccount> toTinkLoans(Credentials credentials);
}
