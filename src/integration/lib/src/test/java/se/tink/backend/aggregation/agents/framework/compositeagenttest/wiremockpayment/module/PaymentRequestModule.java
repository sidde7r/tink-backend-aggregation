package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

public final class PaymentRequestModule extends AbstractModule {

    private final List<Payment> paymentList;
    private final List<Transfer> transferList;

    public PaymentRequestModule(List<Payment> paymentList, List<Transfer> transfersList) {
        this.paymentList = paymentList;
        this.transferList = transfersList;
    }

    @Provides
    @Singleton
    private List<Payment> providePaymentList() {
        return paymentList;
    }

    @Provides
    @Singleton
    private List<Transfer> provideTransferList() {
        return transferList;
    }
}
