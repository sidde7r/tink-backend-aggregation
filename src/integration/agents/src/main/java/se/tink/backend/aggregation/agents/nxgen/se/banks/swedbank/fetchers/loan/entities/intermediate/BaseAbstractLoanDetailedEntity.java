package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.intermediate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.BorrowerEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.ExpenseEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanDetailsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils.SwedbankSeSerializationUtils;
import se.tink.libraries.amount.ExactCurrencyAmount;

public abstract class BaseAbstractLoanDetailedEntity extends BaseAbstractLoanEntity {

    protected BaseAbstractLoanDetailedEntity(LoanEntity loanOverview) {
        super(loanOverview);
    }

    protected BaseAbstractLoanDetailedEntity(
            DetailedLoanResponse loanDetails, LoanEntity loanOverview) {
        super(loanDetails, loanOverview);
    }

    protected List<String> getBorrowers() {
        return loanDetails.map(LoanDetailsAccountEntity::getBorrowers)
                .orElseGet(Collections::emptyList).stream()
                .map(BorrowerEntity::getName)
                .collect(Collectors.toList());
    }

    protected Double getInterest() {
        return SwedbankSeSerializationUtils.parseInterestRate(loanOverview.getInterestRate());
    }

    protected ExactCurrencyAmount getMonthlyAmortization() {
        return allLoanDetails
                .filter(ld -> Objects.nonNull(ld.getUpcomingInvoice()))
                .flatMap(this::getAmortizationAmount)
                .orElse(null);
    }

    private Optional<ExactCurrencyAmount> getAmortizationAmount(DetailedLoanResponse ld) {
        return getAmortizationInvoices(ld).map(ex -> ex.getAmount().getTinkAmount()).findFirst();
    }

    private Stream<ExpenseEntity> getAmortizationInvoices(DetailedLoanResponse ld) {
        return ld.getUpcomingInvoice().getExpenses().stream()
                .filter(ex -> SwedbankSEConstants.AMORTIZATION.equals(ex.getDescription()));
    }
}
