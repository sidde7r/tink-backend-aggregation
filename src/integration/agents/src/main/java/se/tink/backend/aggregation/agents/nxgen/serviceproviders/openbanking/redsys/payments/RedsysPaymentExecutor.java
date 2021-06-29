package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentInitiationRequest.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentInitiationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class RedsysPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final RedsysApiClient apiClient;
    private final RedsysConsentController consentController;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final RedsysPaymentController redsysPaymentController;

    public RedsysPaymentExecutor(
            RedsysApiClient apiClient,
            RedsysConsentController consentController,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.consentController = consentController;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.redsysPaymentController =
                new RedsysPaymentController(
                        supplementalInformationHelper, strongAuthenticationState);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        return apiClient
                .fetchPaymentStatus(payment.getPaymentScheme(), payment.getUniqueId())
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(req -> new PaymentResponse(req.getPayment()))
                        .collect(Collectors.toList()));
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final String consentId = consentController.getConsentId();
        sessionStorage.put(Storage.STATE, strongAuthenticationState.getState());

        Builder requestBuilder = buildPaymentRequest(paymentRequest);

        PaymentInitiationResponse payment =
                apiClient.createPayment(
                        RedsysPaymentType.fromTinkPaymentType(
                                paymentRequest.getPayment().getPaymentScheme()),
                        requestBuilder.build(),
                        consentId);
        sessionStorage.put(Storage.PAYMENT_SCA_REDIRECT, payment.getLinks().getScaRedirectLink());

        return payment.toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        final Payment payment = paymentMultiStepRequest.getPayment();
        String redirectUrl = getScaRedirectUrl(sessionStorage.get(Storage.PAYMENT_SCA_REDIRECT));

        redsysPaymentController.redirectSCA(redirectUrl);
        log.info("SupplementalInformation received and continue to fetchPaymentStatus");

        PaymentStatusResponse paymentStatus =
                apiClient.fetchPaymentStatus(payment.getPaymentScheme(), payment.getUniqueId());
        log.info("Transaction Status: {} after SCA", paymentStatus.getTransactionStatus());

        return redsysPaymentController.response(paymentStatus, paymentMultiStepRequest);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return apiClient.cancelPayment(paymentRequest).toTinkResponse();
    }

    private Builder buildPaymentRequest(PaymentRequest paymentRequest) {
        AccountReferenceEntity creditorAccount =
                new AccountReferenceEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber());
        AccountReferenceEntity debtorAccount =
                new AccountReferenceEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber());
        AmountEntity amount =
                AmountEntity.withAmount(
                        paymentRequest.getPayment().getExactCurrencyAmountFromField());

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        Builder requestBuilder =
                new Builder()
                        .withCreditorAccount(creditorAccount)
                        .withDebtorAccount(debtorAccount)
                        .withInstructedAmount(amount)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withRemittanceInformationUnstructured(remittanceInformation.getValue());
        LocalDate paymentExecutionDate = paymentRequest.getPayment().getExecutionDate();

        if (paymentExecutionDate != null && LocalDate.now().isBefore(paymentExecutionDate)) {
            requestBuilder.withRequestedExecutionDate(paymentExecutionDate.toString());
        }
        return requestBuilder;
    }

    private String getScaRedirectUrl(String redirectUrl) {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(
                        () -> new IllegalStateException("scaRedirect url not present in response"));
    }
}
