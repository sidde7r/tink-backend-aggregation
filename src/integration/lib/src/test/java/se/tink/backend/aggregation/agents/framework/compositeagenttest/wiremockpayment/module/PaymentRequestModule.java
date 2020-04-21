package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.libraries.payment.rpc.Payment;

public final class PaymentRequestModule extends AbstractModule {

    private final List<Payment> paymentList;

    public PaymentRequestModule(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    @Provides
    @Singleton
    private List<Payment> providePaymentList() {
        return paymentList;
    }
}
