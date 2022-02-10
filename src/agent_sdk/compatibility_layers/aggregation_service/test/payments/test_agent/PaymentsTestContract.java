package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.junit.Ignore;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;

@Builder
@Getter
@Ignore
public class PaymentsTestContract {
    @Singular private final List<BulkPaymentRegisterResult> registerResults;
    @Singular private final List<BulkPaymentSignResult> signResults;
    @Singular private final List<BulkPaymentSignResult> signStatusResults;
}
