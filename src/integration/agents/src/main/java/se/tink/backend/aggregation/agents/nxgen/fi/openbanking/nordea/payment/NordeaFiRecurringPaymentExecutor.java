package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.payment;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.PaymentRecurrence;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.PaymentRecurrenceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class NordeaFiRecurringPaymentExecutor extends NordeaBasePaymentExecutor {
    private final NordeaFiSigningController nordeaFiSigningController;
    private final NordeaBaseApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public NordeaFiRecurringPaymentExecutor(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            NordeaFiSigningController nordeaFiSigningController) {
        super(apiClient);
        this.nordeaFiSigningController = nordeaFiSigningController;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    public PaymentResponse createRecurringPayment(PaymentRequest paymentRequest)
            throws PaymentException {

        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getDoubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getCurrencyCode())
                        .withDebtor(debtorEntity)
                        .withExternalId(paymentRequest.getPayment().getUniqueId())
                        .withRecurrence(
                                new PaymentRecurrence(
                                        getRecurrenceCount(paymentRequest),
                                        PaymentRecurrenceType.MONTHLY_SAME_DAY))
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, PaymentType.SEPA)
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        paymentRequest.getPayment().getPaymentServiceType());
    }

    private int getRecurrenceCount(PaymentRequest paymentRequest) {
        return Math.toIntExact(
                ChronoUnit.MONTHS.between(
                        paymentRequest.getPayment().getStartDate(),
                        paymentRequest.getPayment().getEndDate()));
    }

    public PaymentMultiStepResponse signRecurring(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                paymentStatus = initSigning(paymentMultiStepRequest);

                nextStep = SigningStepConstants.STEP_SIGN;
                break;

            case SigningStepConstants.STEP_SIGN:
                nordeaFiSigningController.sign(paymentMultiStepRequest);
                paymentStatus = fetch(paymentMultiStepRequest).getPayment().getStatus();
                nextStep = SigningStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep);
    }

    private PaymentStatus initSigning(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        ConfirmPaymentResponse confirmPaymentsResponse =
                apiClient.confirmPaymentList(
                        Collections.singletonList(
                                paymentMultiStepRequest.getPayment().getUniqueId()));
        PaymentStatus paymentStatus =
                NordeaPaymentStatus.mapToTinkPaymentStatus(
                        NordeaPaymentStatus.fromString(
                                confirmPaymentsResponse.getPaymentResponse().getPaymentStatus()));

        String confirmationLink =
                confirmPaymentsResponse.getLinks().stream()
                        .filter(link -> "signing".equals(link.getRel()))
                        .map(LinkEntity::getHref)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No payment confirmation link found"));
        supplementalInformationController.openThirdPartyAppSync(
                ThirdPartyAppAuthenticationPayload.of(new URL(confirmationLink)));

        return paymentStatus;
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        return PaymentType.SEPA;
    }

    @Override
    protected Collection<PaymentType> getSupportedPaymentTypes() {
        return Collections.singleton(PaymentType.SEPA);
    }

    @Override
    protected Signer getSigner() {
        return nordeaFiSigningController;
    }
}
