package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataLoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@JsonObject
public class GetLoansResponse {

    @JsonProperty("accounts")
    private List<BankdataLoanEntity> loans;

    public List<LoanAccount> getTinkLoans() {
        return loans.stream()
                .filter(accountEntity -> AccountTypes.LOAN == accountEntity.getType())
                .map(BankdataLoanEntity::toTinkLoan)
                .collect(Collectors.toList());
    }

    public List<BankdataLoanEntity> getLoans() {
        return loans;
    }
}
