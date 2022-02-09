package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.libraries.payment.rpc.Payment;

public class BulkPaymentRequestModule extends AbstractModule {
    private final List<Payment> bulkPayment;

    public BulkPaymentRequestModule(List<Payment> bulkPayment) {
        this.bulkPayment = bulkPayment;
    }

    @Provides
    @Singleton
    private List<Payment> provideBulkPayment() {
        return bulkPayment;
    }
}
