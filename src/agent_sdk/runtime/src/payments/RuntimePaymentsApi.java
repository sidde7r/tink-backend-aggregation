package src.agent_sdk.runtime.src.payments;

import java.util.Optional;
import java.util.stream.Stream;
import se.tink.agent.sdk.payments.features.beneficiary.FetchBeneficiaries;
import se.tink.agent.sdk.payments.features.global_signing_basket.DeleteUnsignedPayments;
import src.agent_sdk.runtime.src.instance.AgentInstance;
import src.agent_sdk.runtime.src.payments.beneficiary.RuntimeBeneficiariesFetcher;
import src.agent_sdk.runtime.src.payments.beneficiary.RuntimeBeneficiaryRegistrator;
import src.agent_sdk.runtime.src.payments.beneficiary.processes.generic.GenericBeneficiaryRegistrationProcess;
import src.agent_sdk.runtime.src.payments.bulk.RuntimeBulkPaymentInitiator;
import src.agent_sdk.runtime.src.payments.bulk.processes.generic.GenericBulkPaymentInitiationProcess;
import src.agent_sdk.runtime.src.payments.global_signing_basket.RuntimeUnsignedPaymentsDeleter;
import src.agent_sdk.runtime.src.payments.single.RuntimeSinglePaymentInitiator;
import src.agent_sdk.runtime.src.payments.single.processes.generic.GenericSinglePaymentInitiationProcess;

public class RuntimePaymentsApi {
    private final AgentInstance agentInstance;

    public RuntimePaymentsApi(AgentInstance agentInstance) {
        this.agentInstance = agentInstance;
    }

    public Optional<RuntimeUnsignedPaymentsDeleter> getUnsignedPaymentsDeleter() {
        return this.agentInstance
                .instanceOf(DeleteUnsignedPayments.class)
                .map(DeleteUnsignedPayments::unsignedPaymentsDeleter)
                .map(RuntimeUnsignedPaymentsDeleter::new);
    }

    public Optional<RuntimeBeneficiariesFetcher> getBeneficiariesFetcher() {
        return this.agentInstance
                .instanceOf(FetchBeneficiaries.class)
                .map(FetchBeneficiaries::beneficiariesFetcher)
                .map(RuntimeBeneficiariesFetcher::new);
    }

    public Optional<RuntimeBeneficiaryRegistrator> getBeneficiaryRegistrator() {
        return Stream.of(new GenericBeneficiaryRegistrationProcess())
                .map(process -> process.tryInstantiate(this.agentInstance))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RuntimeBeneficiaryRegistrator::new)
                .findFirst();
    }

    public Optional<RuntimeBulkPaymentInitiator> getBulkPaymentInitiator() {
        return Stream.of(new GenericBulkPaymentInitiationProcess())
                .map(process -> process.tryInstantiate(this.agentInstance))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RuntimeBulkPaymentInitiator::new)
                .findFirst();
    }

    public Optional<RuntimeSinglePaymentInitiator> getSinglePaymentInitiator() {
        return Stream.of(new GenericSinglePaymentInitiationProcess())
                .map(process -> process.tryInstantiate(this.agentInstance))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RuntimeSinglePaymentInitiator::new)
                .findFirst();
    }
}
