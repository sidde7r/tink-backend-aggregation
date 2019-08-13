package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class HandelsbankenPaymentExecutorSelector extends HandelsbankenBasePaymentExecutor {

    public HandelsbankenPaymentExecutorSelector(HandelsbankenBaseApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest) {
        return getSepaOrCrossCurrencyPaymentType(paymentRequest);
    }

    @Override
    public Signer getSigner() {
        throw new NotImplementedException("BankId Signer not implemented for this market.");
    }
}
