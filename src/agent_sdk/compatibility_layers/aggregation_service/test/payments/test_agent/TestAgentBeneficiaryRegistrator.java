package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.beneficiary.steppable_execution.BeneficiarySignFlow;
import se.tink.agent.sdk.payments.beneficiary.steppable_execution.BeneficiarySignStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.libraries.account.AccountIdentifier;

public class TestAgentBeneficiaryRegistrator implements GenericBeneficiaryRegistrator {
    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    public TestAgentBeneficiaryRegistrator(
            PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public BeneficiaryRegisterResult registerBeneficiary(
            AccountIdentifier debtorAccountIdentifier, Beneficiary beneficiary) {
        this.report.addBeneficiaryToRegister(debtorAccountIdentifier, beneficiary);
        return this.contract.getRegisterBeneficiaryResults().entrySet().stream()
                .filter(
                        entry ->
                                debtorAccountIdentifier.equals(entry.getKey().getLeft())
                                        && beneficiary.equals(entry.getKey().getRight()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public BeneficiarySignFlow getSignFlow() {
        return BeneficiarySignFlow.builder()
                .startStep(new TestAgentBeneficiarySignStep(this.report, this.contract))
                .build();
    }

    private static class TestAgentBeneficiarySignStep extends BeneficiarySignStep {
        private final PaymentsTestExecutionReport report;
        private final PaymentsTestContract contract;

        public TestAgentBeneficiarySignStep(
                PaymentsTestExecutionReport report, PaymentsTestContract contract) {
            this.report = report;
            this.contract = contract;
        }

        @Override
        public InteractiveStepResponse<BeneficiaryState> execute(
                StepRequest<BeneficiaryReference> request) {
            BeneficiaryReference beneficiaryReference = request.getStepArgument();

            this.report.addBeneficiaryToSign(
                    beneficiaryReference.getDebtorAccountIdentifier(),
                    beneficiaryReference.getBeneficiary());

            BeneficiaryState beneficiaryState =
                    this.contract.getSignBeneficiaryResults().entrySet().stream()
                            .filter(
                                    entry ->
                                            isSameBeneficiary(beneficiaryReference, entry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);

            return InteractiveStepResponse.done(beneficiaryState);
        }

        private boolean isSameBeneficiary(
                BeneficiaryReference beneficiaryReference, Pair<AccountIdentifier, Beneficiary> b) {
            return beneficiaryReference.getBeneficiary().equals(b.getRight())
                    && beneficiaryReference.getDebtorAccountIdentifier().equals(b.getLeft());
        }
    }
}
