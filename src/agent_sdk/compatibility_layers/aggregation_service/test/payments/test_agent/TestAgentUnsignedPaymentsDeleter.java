package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.List;
import org.junit.Assert;
import se.tink.agent.sdk.models.payments.unsigned_payment.UnsignedPayment;
import se.tink.agent.sdk.payments.global_signing_basket.UnsignedPaymentsDeleter;

public class TestAgentUnsignedPaymentsDeleter implements UnsignedPaymentsDeleter {
    private static final List<UnsignedPayment> UNSIGNED_PAYMENTS =
            List.of(
                    UnsignedPayment.fromBankReference("pay-1"),
                    UnsignedPayment.fromBankReference("pay-2"));

    private final PaymentsTestContract contract;

    public TestAgentUnsignedPaymentsDeleter(PaymentsTestContract contract) {
        this.contract = contract;
    }

    @Override
    public List<UnsignedPayment> getUnsignedPayments() {
        return UNSIGNED_PAYMENTS;
    }

    @Override
    public boolean deleteUnsignedPayments(List<UnsignedPayment> unsignedPayments) {
        Assert.assertEquals(UNSIGNED_PAYMENTS, unsignedPayments);
        return contract.isSuccessfullyDeleteUnsignedPayments();
    }

    @Override
    public boolean deleteUnsignedPayment(UnsignedPayment unsignedPayment) {
        // Noop.
        return false;
    }
}
