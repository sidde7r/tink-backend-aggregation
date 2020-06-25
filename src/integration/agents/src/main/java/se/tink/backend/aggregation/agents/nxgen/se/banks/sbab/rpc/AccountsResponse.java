package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.CallingUserEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.KycEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends StandardResponse {
    private AccountsEntity accounts;
    private CallingUserEntity callingUser;
    private boolean errors;
    private KycEntity kyc;
    private LoansEntity loans;

    public AccountsEntity getAccounts() {
        return Optional.ofNullable(accounts).orElse(new AccountsEntity());
    }

    public CallingUserEntity getCallingUser() {
        return callingUser;
    }

    public boolean isErrors() {
        return errors;
    }

    public KycEntity getKyc() {
        return kyc;
    }

    public LoansEntity getLoans() {
        return loans;
    }
}
