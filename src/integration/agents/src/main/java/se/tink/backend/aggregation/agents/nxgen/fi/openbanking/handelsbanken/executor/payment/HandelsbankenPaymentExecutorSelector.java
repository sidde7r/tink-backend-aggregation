package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

public class HandelsbankenPaymentExecutorSelector extends HandelsbankenBasePaymentExecutor {

    public HandelsbankenPaymentExecutorSelector(HandelsbankenBaseApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest) {
        return getSepaOrCrossCurrencyPaymentType(paymentRequest);
    }
}
