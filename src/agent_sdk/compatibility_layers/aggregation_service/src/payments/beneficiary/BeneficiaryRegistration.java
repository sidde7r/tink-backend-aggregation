package src.agent_sdk.compatibility_layers.aggregation_service.src.payments.beneficiary;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.agent.runtime.models.payments.BeneficiaryReferenceImpl;
import se.tink.agent.runtime.payments.RuntimePaymentsApi;
import se.tink.agent.runtime.payments.beneficiary.RuntimeBeneficiariesFetcher;
import se.tink.agent.runtime.payments.beneficiary.RuntimeBeneficiaryRegistrator;
import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.ConnectivityError;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.libraries.account.AccountIdentifier;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.src.steppable_execution.AggregationServiceSteppableExecutor;

public class BeneficiaryRegistration {
    private final AggregationServiceSteppableExecutor steppableExecutor;
    private final RuntimePaymentsApi runtimePaymentsApi;

    public BeneficiaryRegistration(
            AggregationServiceSteppableExecutor steppableExecutor,
            RuntimePaymentsApi runtimePaymentsApi) {
        this.steppableExecutor = steppableExecutor;
        this.runtimePaymentsApi = runtimePaymentsApi;
    }

    public List<PaymentInitiationState> registerBeneficiariesForPayments(List<Payment> payments) {
        Optional<RuntimeBeneficiariesFetcher> maybeBeneficiariesFetcher =
                this.runtimePaymentsApi.getBeneficiariesFetcher();
        Optional<RuntimeBeneficiaryRegistrator> maybeBeneficiaryRegistrator =
                this.runtimePaymentsApi.getBeneficiaryRegistrator();

        if (maybeBeneficiariesFetcher.isEmpty() || maybeBeneficiaryRegistrator.isEmpty()) {
            // Return early with a successful result if the agent does not implement both
            // beneficiary fetcher and registrator.
            return payments.stream()
                    .map(
                            payment ->
                                    PaymentInitiationState.builder()
                                            .paymentReference(
                                                    PaymentReference.builder()
                                                            .payment(payment)
                                                            .noBankReference()
                                                            .build())
                                            .build())
                    .collect(Collectors.toList());
        }

        BeneficiariesCache cache = new BeneficiariesCache();
        return payments.stream()
                .filter(payment -> payment.tryGetDebtor().isPresent())
                .map(
                        payment ->
                                registerBeneficiaryForPayment(
                                        payment,
                                        cache,
                                        maybeBeneficiariesFetcher.get(),
                                        maybeBeneficiaryRegistrator.get()))
                .collect(Collectors.toList());
    }

    public PaymentInitiationState registerBeneficiaryForPayment(Payment payment) {
        Optional<RuntimeBeneficiariesFetcher> maybeBeneficiariesFetcher =
                this.runtimePaymentsApi.getBeneficiariesFetcher();
        Optional<RuntimeBeneficiaryRegistrator> maybeBeneficiaryRegistrator =
                this.runtimePaymentsApi.getBeneficiaryRegistrator();

        if (maybeBeneficiariesFetcher.isEmpty() || maybeBeneficiaryRegistrator.isEmpty()) {
            // Return early with a successful result if the agent does not implement both
            // beneficiary fetcher and registrator.
            return PaymentInitiationState.builder()
                    .paymentReference(
                            PaymentReference.builder().payment(payment).noBankReference().build())
                    .build();
        }
        BeneficiariesCache cache = new BeneficiariesCache();
        return registerBeneficiaryForPayment(
                payment, cache, maybeBeneficiariesFetcher.get(), maybeBeneficiaryRegistrator.get());
    }

    private PaymentInitiationState registerBeneficiaryForPayment(
            Payment payment,
            BeneficiariesCache cache,
            RuntimeBeneficiariesFetcher beneficiariesFetcher,
            RuntimeBeneficiaryRegistrator beneficiaryRegistrator) {

        PaymentReference paymentReference =
                PaymentReference.builder().payment(payment).noBankReference().build();

        Debtor debtor = payment.getDebtor();
        Creditor creditor = payment.getCreditor();

        AccountIdentifier debtorAccountIdentifier = debtor.getAccountIdentifier();
        AccountIdentifier creditorAccountIdentifier = creditor.getAccountIdentifier();

        if (cache.contains(debtorAccountIdentifier, creditorAccountIdentifier)) {
            // Creditor is already cached, no need to re-list/-register.
            return PaymentInitiationState.builder().paymentReference(paymentReference).build();
        }

        if (!cache.hasCached(debtorAccountIdentifier)) {
            // Only fetch beneficiaries for a debtorAccountIdentifier once.
            // There's no point in fetching beneficiaries for the same account multiple times, the
            // cache contain the result from the first fetch.
            Set<AccountIdentifier> registeredBeneficiaries =
                    beneficiariesFetcher.fetchPaymentBeneficiariesFor(debtorAccountIdentifier)
                            .stream()
                            .map(Beneficiary::getAccountIdentifier)
                            .collect(Collectors.toSet());

            cache.add(debtorAccountIdentifier, registeredBeneficiaries);

            if (cache.contains(debtorAccountIdentifier, creditorAccountIdentifier)) {
                // The creditor is among the newly fetched payment beneficiaries.
                return PaymentInitiationState.builder().paymentReference(paymentReference).build();
            }
        }

        Beneficiary beneficiaryToRegister =
                Beneficiary.builder()
                        .name(creditor.getName())
                        .accountIdentifier(creditorAccountIdentifier)
                        .build();

        Optional<ConnectivityError> connectivityError =
                registerAndSignBeneficiary(
                        beneficiaryRegistrator, debtorAccountIdentifier, beneficiaryToRegister);
        if (connectivityError.isPresent()) {
            return PaymentInitiationState.builder()
                    .paymentReference(paymentReference)
                    .error(connectivityError.get())
                    .build();
        }

        cache.add(debtorAccountIdentifier, beneficiaryToRegister.getAccountIdentifier());

        return PaymentInitiationState.builder().paymentReference(paymentReference).build();
    }

    private Optional<ConnectivityError> registerAndSignBeneficiary(
            RuntimeBeneficiaryRegistrator beneficiaryRegistrator,
            AccountIdentifier debtorAccountIdentifier,
            Beneficiary beneficiary) {

        BeneficiaryRegisterResult registerResult =
                beneficiaryRegistrator.registerBeneficiary(debtorAccountIdentifier, beneficiary);

        if (registerResult.getError().isPresent()) {
            return registerResult.getError();
        }

        BeneficiaryReference beneficiaryReference =
                new BeneficiaryReferenceImpl(
                        beneficiary, registerResult.getBankReference().orElse(null));

        BeneficiaryState beneficiaryState =
                this.steppableExecutor.execute(
                        beneficiaryRegistrator.getSignFlow(), beneficiaryReference);

        return beneficiaryState.getError();
    }
}
