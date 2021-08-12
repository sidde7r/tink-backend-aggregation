package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class LclPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    @Override
    public LocalDate apply(Payment payment) {
        if (payment.getExecutionDate() != null) {
            return payment.getExecutionDate();
        }
        LocalDateTime created = FrOpenBankingDateUtil.getCreationDate();
        return created.plusDays(1L).toLocalDate();
    }
}
