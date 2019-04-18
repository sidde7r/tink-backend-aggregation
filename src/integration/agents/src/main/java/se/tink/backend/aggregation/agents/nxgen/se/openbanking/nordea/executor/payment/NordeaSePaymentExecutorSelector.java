package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.stream.Collector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaSePaymentExecutorSelector implements PaymentExecutor {
    private ImmutableMap<PaymentType, PaymentExecutor> paymentTypeToExecutorMap;
    private ImmutableSet<PaymentExecutor> executorsSet;

    // The key is a pair where the key is debtor account type and value is creditor account type.
    // The mapping follows the instructions in:
    // https://developer.nordeaopenbanking.com/app/documentation?api=Payments%20API%20Domestic%20transfer&version=3.3#payment_types_field_combinations
    private static final ImmutableMap<
                    Pair<AccountIdentifier.Type, AccountIdentifier.Type>, PaymentType>
            accountIdentifiersToPaymentTypeMap =
                    ImmutableMap
                            .<Pair<AccountIdentifier.Type, AccountIdentifier.Type>, PaymentType>
                                    builder()
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.IBAN),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_BG),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_PG),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN),
                                    PaymentType.SEPA)
                            .build();;

    public NordeaSePaymentExecutorSelector(NordeaBaseApiClient apiClient) {
        NordeaSeDomesticPaymentExecutor nordeaSeDomesticPaymentExecutor =
                new NordeaSeDomesticPaymentExecutor(apiClient);

        this.executorsSet =
                ImmutableSet.<PaymentExecutor>builder()
                        .add(nordeaSeDomesticPaymentExecutor)
                        .build();

        this.paymentTypeToExecutorMap =
                ImmutableMap.<PaymentType, PaymentExecutor>builder()
                        .put(PaymentType.DOMESTIC, nordeaSeDomesticPaymentExecutor)
                        .build();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).create(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).fetch(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        return getRelevantExecutor(paymentMultiStepRequest).sign(paymentMultiStepRequest);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).cancel(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest) {
        Collector<PaymentListResponse, ArrayList<PaymentResponse>, PaymentListResponse>
                paymentListResponseCollector =
                        Collector.of(
                                () -> new ArrayList<>(),
                                (paymentResponses, paymentListResponse) ->
                                        paymentResponses.addAll(
                                                paymentListResponse.getPaymentResponseList()),
                                (paymentResponses1, paymentResponses2) -> {
                                    paymentResponses1.addAll(paymentResponses2);
                                    return paymentResponses1;
                                },
                                paymentResponses -> new PaymentListResponse(paymentResponses));

        return executorsSet.stream()
                .map(executor -> executor.fetchMultiple(null))
                .collect(paymentListResponseCollector);
    }

    private PaymentExecutor getRelevantExecutor(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();
        PaymentType paymentType = accountIdentifiersToPaymentTypeMap.get(accountIdentifiersKey);
        PaymentExecutor relevantPaymentExecutor = paymentTypeToExecutorMap.get(paymentType);
        if (relevantPaymentExecutor == null) {
            throw new NotImplementedException(
                    "No relevant PayentExecutor found for your AccountIdentifiers pair "
                            + accountIdentifiersKey);
        }
        return relevantPaymentExecutor;
    }
}
