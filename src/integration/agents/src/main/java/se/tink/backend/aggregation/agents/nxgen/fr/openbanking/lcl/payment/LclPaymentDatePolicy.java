package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class LclPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    @Override
    public LocalDate apply(Payment payment) {
        if (payment.getExecutionDate() != null) {
            return payment.getExecutionDate();
        }
        AccountEntity creditor = AccountEntity.creditorOf(payment);
        LocalDateTime created = FrOpenBankingDateUtil.getCreationDate();
        if (creditor.isMonacoIban() || creditor.isFrenchIban()) {
            return created.toLocalDate();
        } else {
            return created.plusDays(1L).toLocalDate();
        }
    }
}
