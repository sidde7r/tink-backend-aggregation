package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import io.vavr.CheckedFunction1;
import io.vavr.Value;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CancelPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public abstract class NordeaBasePaymentExecutor
        implements PaymentExecutor, FetchablePaymentExecutor {
    private NordeaBaseApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;

    private static final CountryDateHelper dateHelper = new CountryDateHelper(Locale.getDefault());

    public NordeaBasePaymentExecutor(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public NordeaBasePaymentExecutor(
            NordeaBaseApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);

        Payment payment = paymentRequest.getPayment();
        if (Objects.isNull(payment.getExecutionDate())) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getDoubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withDebtor(debtorEntity)
                        .withExecutionDate(
                                paymentRequest.getPayment().getExecutionDate().toString())
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, getPaymentType(paymentRequest))
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        paymentRequest.getPayment().getPaymentServiceType());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(), getPaymentType(paymentRequest))
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        paymentRequest.getPayment().getPaymentServiceType());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                List<String> paymentIds = new ArrayList<>();
                paymentIds.add(paymentMultiStepRequest.getPayment().getUniqueId());
                ConfirmPaymentRequest confirmPaymentRequest =
                        ConfirmPaymentRequest.builder()
                                .paymentIds(paymentIds)
                                .state(strongAuthenticationState.getState())
                                .build();

                ConfirmPaymentResponse confirmPaymentsResponse =
                        apiClient.confirmPayment(
                                confirmPaymentRequest, getPaymentType(paymentMultiStepRequest));

                String href =
                        confirmPaymentsResponse.getLinks().stream()
                                .filter(link -> "signing".equals(link.getRel()))
                                .map(LinkEntity::getHref)
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "No payment confirmation link found"));

                this.supplementalInformationHelper.openThirdPartyApp(
                        ThirdPartyAppAuthenticationPayload.of(URL.of(href)));

                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

                paymentStatus =
                        NordeaPaymentStatus.mapToTinkPaymentStatus(
                                NordeaPaymentStatus.fromString(
                                        confirmPaymentsResponse
                                                .getPaymentResponse()
                                                .getPaymentStatus()));
                nextStep = SigningStepConstants.STEP_SIGN;
                break;

            case SigningStepConstants.STEP_SIGN:
                try {
                    getSigner().sign(paymentMultiStepRequest);
                } catch (AuthenticationException e) {
                    if (e instanceof BankIdException) {
                        BankIdError bankIdError = ((BankIdException) e).getError();
                        switch (bankIdError) {
                            case CANCELLED:
                                throw new PaymentAuthorizationException(
                                        "BankId signing cancelled by the user.", e);

                            case NO_CLIENT:
                                throw new PaymentAuthorizationException(
                                        "No BankId client when trying to sign the payment.", e);

                            case TIMEOUT:
                                throw new PaymentAuthorizationException(
                                        "BankId signing timed out.", e);

                            case INTERRUPTED:
                                throw new PaymentAuthorizationException(
                                        "BankId signing interrupded.", e);

                            case UNKNOWN:
                            default:
                                throw new PaymentAuthorizationException(
                                        "Unknown problem when signing payment with BankId.", e);
                        }
                    }
                }
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

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) throws PaymentException {
        Payment payment = paymentRequest.getPayment();
        PaymentType paymentType = payment.getType();
        String paymentId = payment.getUniqueId();
        apiClient.getPayment(paymentId, paymentType);
        CancelPaymentResponse cancelPaymentResponse =
                apiClient.deletePayment(paymentId, paymentType);

        if ((cancelPaymentResponse.getResponse().isEmpty())
                && cancelPaymentResponse.getResponse().get(0).equals(paymentId)) {
            try {
                GetPaymentResponse paymentResponse = apiClient.getPayment(paymentId, paymentType);
                return paymentResponse.toTinkPaymentResponse(
                        paymentType, payment.getPaymentServiceType());
            } catch (HttpResponseException hre) {
                if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                    return cancelPaymentResponse.toTinkCancellablePaymentResponseWithStatus(
                            NordeaPaymentStatus.CANCELLED, payment);
                }
            }
        }

        return cancelPaymentResponse.toTinkCancellablePaymentResponseWithStatus(
                NordeaPaymentStatus.UNKNOWN, payment);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        Collector<PaymentListResponse, ArrayList<PaymentResponse>, PaymentListResponse>
                paymentListResponseCollector =
                        Collector.of(
                                ArrayList::new,
                                (paymentResponses, paymentListResponse) ->
                                        paymentResponses.addAll(
                                                paymentListResponse.getPaymentResponseList()),
                                (paymentResponses1, paymentResponses2) -> {
                                    paymentResponses1.addAll(paymentResponses2);
                                    return paymentResponses1;
                                },
                                PaymentListResponse::new);

        List<Try<PaymentListResponse>> allTries =
                getSupportedPaymentTypes().stream()
                        .map(
                                CheckedFunction1.liftTry(
                                        paymentType ->
                                                apiClient
                                                        .fetchPayments(paymentType)
                                                        .toTinkPaymentListResponse(paymentType)))
                        .collect(Collectors.toList());

        List<Try<PaymentListResponse>> failedTries =
                allTries.stream().filter(Try::isFailure).collect(Collectors.toList());

        if (!failedTries.isEmpty()) {
            Throwable failedTryCause = failedTries.stream().findFirst().get().getCause();
            if (failedTryCause instanceof PaymentException) {
                throw (PaymentException) failedTryCause;
            } else {
                throw new PaymentException(
                        "Unrecognized exception when fetching multiple payments.", failedTryCause);
            }
        }

        return allTries.stream().flatMap(Value::toJavaStream).collect(paymentListResponseCollector);
    }

    protected abstract PaymentType getPaymentType(PaymentRequest paymentRequest);

    protected abstract Collection<PaymentType> getSupportedPaymentTypes();

    protected abstract Signer getSigner();
}
