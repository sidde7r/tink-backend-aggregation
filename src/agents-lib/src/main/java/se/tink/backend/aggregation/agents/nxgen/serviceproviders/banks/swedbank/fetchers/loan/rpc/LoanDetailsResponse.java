package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

@JsonObject
public class LoanDetailsResponse {
    private LoanDetailsAccountEntity loanAccount;
    private LoanPaymentEntity loanPayment;

    public LoanDetailsAccountEntity getLoanAccount() {
        return loanAccount;
    }

    public LoanPaymentEntity getLoanPayment() {
        return loanPayment;
    }

    public Optional<String> getInterest() {
        return Optional.ofNullable(loanAccount)
                .map(LoanDetailsAccountEntity::getInterest);
    }

    public Optional<Date> getDueDate() {
        return Optional.ofNullable(loanPayment)
                .map(LoanPaymentEntity::getDueDay);
    }
}
