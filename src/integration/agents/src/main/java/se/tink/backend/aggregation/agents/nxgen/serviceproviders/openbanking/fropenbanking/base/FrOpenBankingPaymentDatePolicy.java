package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class FrOpenBankingPaymentDatePolicy {

    public LocalDate apply(Payment payment) {
        return FrOpenBankingDateUtil.getExecutionDate(payment.getExecutionDate());
    }
}
