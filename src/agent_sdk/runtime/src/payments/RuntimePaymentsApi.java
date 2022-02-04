package se.tink.agent.runtime.payments;

import java.util.Optional;
import java.util.stream.Stream;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.payments.beneficiary.RuntimeBeneficiariesFetcher;
import se.tink.agent.runtime.payments.beneficiary.RuntimeBeneficiaryRegistrator;
import se.tink.agent.runtime.payments.beneficiary.processes.generic.GenericBeneficiaryRegistrationProcess;
import se.tink.agent.runtime.payments.bulk.RuntimeBulkPaymentInitiator;
import se.tink.agent.runtime.payments.bulk.processes.generic.GenericBulkPaymentInitiationProcess;
import se.tink.agent.runtime.payments.global_signing_basket.RuntimeUnsignedPaymentsDeleter;
import se.tink.agent.runtime.payments.single.RuntimeSinglePaymentInitiator;
import se.tink.agent.runtime.payments.single.processes.generic.GenericSinglePaymentInitiationProcess;
import se.tink.agent.sdk.payments.features.beneficiary.FetchBeneficiaries;
import se.tink.agent.sdk.payments.features.global_signing_basket.DeleteUnsignedPayments;

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
