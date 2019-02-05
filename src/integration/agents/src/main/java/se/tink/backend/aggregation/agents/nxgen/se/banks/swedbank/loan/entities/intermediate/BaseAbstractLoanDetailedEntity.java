package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.BorrowerEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.libraries.amount.Amount;

public abstract class BaseAbstractLoanDetailedEntity extends BaseAbstractLoanEntity {

    protected BaseAbstractLoanDetailedEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    protected BaseAbstractLoanDetailedEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    protected List<String> getBorrowers() {
        return loanDetails
                .map(LoanDetailsAccountEntity::getBorrowers).orElseGet(Collections::emptyList)
                .stream()
                .map(BorrowerEntity::getName)
                .collect(Collectors.toList());
    }

    protected Double getInterest() {
        return SwedbankSeSerializationUtils.parseInterestRate(loanOverview.getInterestRate());
    }

    protected Amount getMonthlyAmortization() {
        return allLoanDetails
                .filter(ld -> Objects.nonNull(ld.getUpcomingInvoice()))
                .map(ld -> ld.getUpcomingInvoice().getExpenses().stream()
                        .filter(ex -> SwedbankSEConstants.AMORTIZATION.equals(ex.getDescription()))
                        .map(ex -> ex.getAmount().getTinkAmount()).findFirst().orElse(null))
                .orElse(null);
    }
}
