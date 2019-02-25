package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc;

public class ExecutePaymentResponse {

    private String paymentOrderUid;
    private String paymentUid;

    public String getPaymentOrderUid() {
        return paymentOrderUid;
    }

    public String getPaymentUid() {
        return paymentUid;
    }
}
