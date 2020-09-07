package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.CallingUserEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.KycEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse extends StandardResponse {
    private AccountsEntity accounts = new AccountsEntity();
    private CallingUserEntity callingUser;
    private boolean errors;
    private KycEntity kyc;
    private LoansEntity loans;
}
