package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public class HandelsbankenPaymentExecutorSelector extends HandelsbankenBasePaymentExecutor {

    public HandelsbankenPaymentExecutorSelector(HandelsbankenBaseApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest) {
        return getSepaOrCrossCurrencyPaymentType(paymentRequest);
    }

    @Override
    protected AccountEntity getDebtorAccountEntity(Payment payment) {
        throw new NotImplementedException(
                "getDebtorAccountEntity not implemented for this market.");
    }

    @Override
    protected AccountEntity getCreditorAccountEntity(Creditor creditor) {
        throw new NotImplementedException(
                "getCreditorAccountEntity not implemented for this market.");
    }

    @Override
    public Signer getSigner() {
        throw new NotImplementedException("BankId Signer not implemented for this market.");
    }
}
