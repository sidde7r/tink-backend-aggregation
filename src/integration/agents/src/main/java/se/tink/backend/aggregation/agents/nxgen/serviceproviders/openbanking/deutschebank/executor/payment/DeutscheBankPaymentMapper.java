package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.executor.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.libraries.payment.rpc.Payment;

public class DeutscheBankPaymentMapper extends BasePaymentMapper {

    @Override
    protected AccountEntity getDebtorAccountEntity(Payment payment) {
        return new AccountEntity(payment.getDebtor().getAccountNumber(), payment.getCurrency());
    }

    @Override
    protected AccountEntity getCreditorAccountEntity(Payment payment) {
        return new AccountEntity(payment.getCreditor().getAccountNumber());
    }
}
