package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.libraries.account.AccountIdentifier;

@Builder
@Getter
public class PaymentsTestContract {
    @Singular private final List<BulkPaymentRegisterResult> registerResults;
    @Singular private final List<BulkPaymentSignResult> signResults;
    @Singular private final List<BulkPaymentSignResult> signStatusResults;
    @Singular private final Map<AccountIdentifier, List<Beneficiary>> fetchBeneficiaryResults;

    @Singular
    private final Map<Pair<AccountIdentifier, Beneficiary>, BeneficiaryRegisterResult>
            registerBeneficiaryResults;

    @Singular
    private final Map<Pair<AccountIdentifier, Beneficiary>, BeneficiaryState>
            signBeneficiaryResults;
}
