package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder;

import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public interface TypeStep<T> {

    BalanceStep<T> withType(Type loanType);
}
