package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.core.Amount;

public abstract class BaseAbstractLoanDetailedEntity extends BaseAbstractLoanEntity {
    public static final String AMORTIZATION = "Amorteringsbelopp";

    protected BaseAbstractLoanDetailedEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    protected BaseAbstractLoanDetailedEntity(DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    protected List<String> getBorrowers() {
        return loanDetails.map(ld -> ld.getBorrowers())
                .orElseGet(Collections::emptyList)
                .stream().map(b -> b.getName())
                .collect(Collectors.toList());
    }

    protected Double getInterest() {
        return SwedbankSeSerializationUtils.parseInterestRate(loanOverview.getInterestRate());
    }

    protected Amount getMonthlyAmortization() {
        return allLoanDetails
                .map(ld -> ld.getUpcomingInvoice().getExpenses().stream()
                        .filter(ex -> AMORTIZATION.equals(ex.getDescription()))
                        .map(ex -> ex.getAmount().getTinkAmount()).findFirst().orElseGet(() -> null))
                .orElseGet(() -> null);
    }
}
