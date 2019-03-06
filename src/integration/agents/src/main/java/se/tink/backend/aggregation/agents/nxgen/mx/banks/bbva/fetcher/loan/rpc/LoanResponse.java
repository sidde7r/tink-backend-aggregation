package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.rpc;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity.LoanDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@JsonObject
public class LoanResponse {
    private LoanDataEntity data;

    public Collection<LoanAccount> getLoanAccounts() {
    	return data.getLoanAccounts();
	}
}
