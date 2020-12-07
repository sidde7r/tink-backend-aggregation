package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.libraries.payment.rpc.Payment;

public final class PaymentRequestModule extends AbstractModule {

    private final Payment payment;

    public PaymentRequestModule(Payment payment) {
        this.payment = payment;
    }

    @Provides
    @Singleton
    private Payment providePayment() {
        return payment;
    }
}
