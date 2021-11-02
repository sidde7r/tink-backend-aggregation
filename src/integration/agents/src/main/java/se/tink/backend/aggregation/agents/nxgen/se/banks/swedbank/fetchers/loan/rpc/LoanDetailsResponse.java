package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanDetailsResponse {
    private LoanDetailsAccountEntity loanAccount;
    private LoanPaymentEntity loanPayment;

    public Optional<String> getInterest() {
        return Optional.ofNullable(loanAccount).map(LoanDetailsAccountEntity::getInterest);
    }

    public Optional<Date> getDueDate() {
        return Optional.ofNullable(loanPayment).map(LoanPaymentEntity::getDueDay);
    }
}
