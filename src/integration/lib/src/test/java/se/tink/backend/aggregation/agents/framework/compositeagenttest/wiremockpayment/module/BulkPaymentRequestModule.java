package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

public class BulkPaymentRequestModule extends AbstractModule {
    private final List<Payment> bulkPayment;
    private final List<Transfer> bulkTransfer;

    public BulkPaymentRequestModule(List<Payment> bulkPayment, final List<Transfer> bulkTransfer) {
        this.bulkPayment = bulkPayment;
        this.bulkTransfer = bulkTransfer;
    }

    @Provides
    @Singleton
    private List<Payment> provideBulkPayment() {
        return bulkPayment;
    }

    @Provides
    @Singleton
    private List<Transfer> provideBulkTransfer() {
        return bulkTransfer;
    }
}
