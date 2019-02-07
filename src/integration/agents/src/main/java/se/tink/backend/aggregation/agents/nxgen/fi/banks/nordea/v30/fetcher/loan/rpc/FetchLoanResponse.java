package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@JsonObject
public class FetchLoanResponse {

    private List<LoanAccountEntity> products;

    public List<LoanAccount> toTinkLoanAccounts() {

        return products.stream()
                .map(LoanAccountEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
