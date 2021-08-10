package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.libraries.payment.rpc.Payment;

public class BpceGroupPaymentDatePolicy extends FrOpenBankingPaymentDatePolicy {

    private final boolean isCaisseGroup;

    public BpceGroupPaymentDatePolicy(String providerName) {
        isCaisseGroup = providerName.startsWith("fr-caisse");
    }

    @Override
    public LocalDate apply(Payment payment) {
        if (payment.getExecutionDate() != null) {
            return payment.getExecutionDate();
        } else {
            LocalDate createDate = FrOpenBankingDateUtil.getCreationDate().toLocalDate();
            if (isCaisseGroup && !FrOpenBankingDateUtil.isBusinessDate(createDate)) {
                return super.apply(payment).plusDays(3);
            } else {
                return super.apply(payment);
            }
        }
    }
}
