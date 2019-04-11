package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.intermediate;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;

public class LoanEntityFactory {

    public <T extends BaseAbstractLoanEntity> T create(
            Class<T> type, LoanEntity loanOverview, DetailedLoanResponse loanDetails) {

        if (CarLoanEntity.class.equals(type)) {
            return (T) CarLoanEntity.create(loanOverview, loanDetails);
        }
        if (CollateralsLoanEntity.class.equals(type)) {
            return (T) CollateralsLoanEntity.create(loanOverview, loanDetails);
        }
        if (ConsumptionLoanEntity.class.equals(type)) {
            return (T) ConsumptionLoanEntity.create(loanOverview, loanDetails);
        }
        return null;
    }

    public <T extends BaseAbstractLoanEntity> T create(Class<T> type, LoanEntity loan) {
        if (CarLoanEntity.class.equals(type)) {
            return (T) CarLoanEntity.create(loan);
        }
        if (CollateralsLoanEntity.class.equals(type)) {
            return (T) CollateralsLoanEntity.create(loan);
        }
        if (ConsumptionLoanEntity.class.equals(type)) {
            return (T) ConsumptionLoanEntity.create(loan);
        }
        return null;
    }
}
