package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RequiredArgsConstructor
public class FinecoBankPaymentExecutor implements PaymentExecutor {

    private static final String PAYMENT_POST_SIGN_STATE = "payment_post_sign_state";

    private final FinecoBankApiClient apiClient;
    private final FinecoStorage storage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        CreatePaymentRequest createPaymentRequest = buildCreatePaymentRequest(payment);

        FinecoBankPaymentProduct finecoProduct = FinecoBankPaymentProduct.fromTinkPayment(payment);

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        createPaymentRequest, finecoProduct, strongAuthenticationState.getState());
        String paymentId = createPaymentResponse.getPaymentId();
        GetPaymentAuthsResponse paymentAuths = apiClient.getPaymentAuths(finecoProduct, paymentId);

        validateCreatedPayment(createPaymentResponse, paymentAuths);

        storage.storePaymentAuthId(paymentId, paymentAuths.getAuthorisationIds().get(0));
        storage.storePaymentAuthorizationUrl(paymentId, createPaymentResponse.getScaRedirectLink());
        return createPaymentResponse.toTinkPaymentResponse(paymentRequest);
    }

    private CreatePaymentRequest buildCreatePaymentRequest(Payment payment) {
        AccountEntity creditorAccount = new AccountEntity(payment.getCreditor().getAccountNumber());
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, RemittanceInformationType.UNSTRUCTURED);
        AmountEntity amountEntity = new AmountEntity(payment.getExactCurrencyAmountFromField());
        return CreatePaymentRequest.builder()
                .creditorAccount(creditorAccount)
                .creditorName(payment.getCreditor().getName())
                .instructedAmount(amountEntity)
                .remittanceInformationUnstructured(remittanceInformation.getValue())
                .build();
    }

    private void validateCreatedPayment(
            CreatePaymentResponse createPaymentResponse,
            GetPaymentAuthsResponse getPaymentAuthsResponse) {
        if (createPaymentResponse.getScaRedirectLink() == null) {
            throw new IllegalStateException(
                    "CreatePaymentResponse received from bank is incorrect, missing SCA link!");
        }

        // Each payment in not-business setting should have exactly one auth entity attached to it.
        if (getPaymentAuthsResponse.getAuthorisationIds().size() != 1) {
            throw new IllegalStateException(
                    "CreatePaymentResponse received from bank is incorrect, could not find just one authId!");
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                nextStep = handleInitStep(payment.getUniqueId());
                break;
            case PAYMENT_POST_SIGN_STATE:
                nextStep = handlePostSignStep(payment);
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.UNKNOWN_SIGNING_STEP,
                                paymentMultiStepRequest.getStep()));
        }
        return new PaymentMultiStepResponse(payment, nextStep, Collections.emptyList());
    }

    private String handleInitStep(String paymentId) {
        supplementalInformationController.openThirdPartyAppSync(
                ThirdPartyAppAuthenticationPayload.of(
                        new URL(storage.getPaymentAuthorizationUrl(paymentId))));
        return PAYMENT_POST_SIGN_STATE;
    }

    private String handlePostSignStep(Payment payment) throws PaymentException {
        String paymentId = payment.getUniqueId();
        FinecoBankPaymentProduct finecoProduct = FinecoBankPaymentProduct.fromTinkPayment(payment);

        GetPaymentAuthStatusResponse paymentAuthStatus =
                apiClient.getPaymentAuthStatus(
                        finecoProduct, paymentId, storage.getPaymentAuthId(paymentId));
        if (!paymentAuthStatus.authFinishedSuccessfully()) {
            throw new PaymentAuthorizationException();
        }
        apiClient.getPayment(finecoProduct, paymentId);
        GetPaymentStatusResponse paymentStatus =
                apiClient.getPaymentStatus(finecoProduct, paymentId);
        PaymentStatus tinkPaymentStatus =
                FinecoBankPaymentStatus.mapToTinkPaymentStatus(
                        FinecoBankPaymentStatus.fromString(paymentStatus.getTransactionStatus()));
        switch (tinkPaymentStatus) {
            case CREATED:
            case REJECTED:
                throw new PaymentAuthorizationException();
            case CANCELLED:
                throw new PaymentCancelledException();
            default:
                payment.setStatus(tinkPaymentStatus);
        }

        return AuthenticationStepConstants.STEP_FINALIZE;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBenficiary is not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new IllegalStateException(
                "cancel is not implemented for " + this.getClass().getName());
    }
}
