package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;

@JsonObject
public class MortgageEntity extends BaseLoanEntity {
    private String lastPaymentDate;
    private AmountEntity grantedAmount;
    private AmountEntity pendingBalance;

    @JsonIgnore
    public Optional<LoanAccount> toTinkMortgage(LoanDetailsResponse loanDetails) {

        Optional<LoanModule> loanModule =
                loanDetails.getLoanModuleWithTypeAndLoanNumber(Type.MORTGAGE, getAccountNumber());

        return loanModule.map(
                module ->
                        LoanAccount.nxBuilder()
                                .withLoanDetails(module)
                                .withId(getIdModule())
                                .setApiIdentifier(getId())
                                .build());
    }

    @Override
    protected String getAccountNumber() {
        return getFormats().getBocf();
    }
}
