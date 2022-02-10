package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import se.tink.agent.sdk.models.payments.payment.Payment;

@Ignore
public class PaymentsTestExecutionReport {
    private final List<Payment> paymentsAskedToRegister = new ArrayList<>();
    private final List<Payment> paymentsAskedToSign = new ArrayList<>();
    private final Set<Payment> paymentsAskedToGetSignStatus = new HashSet<>();

    public List<Payment> getPaymentsAskedToRegister() {
        return this.paymentsAskedToRegister;
    }

    public List<Payment> getPaymentsAskedToSign() {
        return this.paymentsAskedToSign;
    }

    public Set<Payment> getPaymentsAskedToGetSignStatus() {
        return this.paymentsAskedToGetSignStatus;
    }

    public void addPaymentsToRegister(List<Payment> payments) {
        this.paymentsAskedToRegister.addAll(payments);
    }

    public void addPaymentsToSign(List<Payment> payments) {
        this.paymentsAskedToSign.addAll(payments);
    }

    public void addPaymentsToGetSignStatus(List<Payment> payments) {
        this.paymentsAskedToGetSignStatus.addAll(payments);
    }
}
