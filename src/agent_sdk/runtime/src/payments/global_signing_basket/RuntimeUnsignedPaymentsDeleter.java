package src.agent_sdk.runtime.src.payments.global_signing_basket;

import java.util.List;
import se.tink.agent.sdk.models.payments.unsigned_payment.UnsignedPayment;
import se.tink.agent.sdk.payments.global_signing_basket.UnsignedPaymentsDeleter;

public class RuntimeUnsignedPaymentsDeleter {
    private final UnsignedPaymentsDeleter agentUnsignedPaymentsDeleter;

    public RuntimeUnsignedPaymentsDeleter(UnsignedPaymentsDeleter agentUnsignedPaymentsDeleter) {
        this.agentUnsignedPaymentsDeleter = agentUnsignedPaymentsDeleter;
    }

    public List<UnsignedPayment> getUnsignedPayments() {
        return this.agentUnsignedPaymentsDeleter.getUnsignedPayments();
    }

    public boolean deleteUnsignedPayments(List<UnsignedPayment> unsignedPayments) {
        return this.agentUnsignedPaymentsDeleter.deleteUnsignedPayments(unsignedPayments);
    }
}
