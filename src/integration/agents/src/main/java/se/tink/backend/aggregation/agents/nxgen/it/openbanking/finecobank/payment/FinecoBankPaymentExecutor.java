package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
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
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RequiredArgsConstructor
public class FinecoBankPaymentExecutor implements PaymentExecutor {

    static final String PAYMENT_POST_SIGN_STATE = "payment_post_sign_state";
    private final FinecoBankApiClient apiClient;
    private final FinecoStorage storage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amountEntity =
                new AmountEntity(paymentRequest.getPayment().getExactCurrencyAmountFromField());

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        CreatePaymentRequest requestBody =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorEntity)
                        .debtorAccount(debtorEntity)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .instructedAmount(amountEntity)
                        .remittanceInformationUnstructured(remittanceInformation.getValue())
                        .build();

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        FinecoBankPaymentProduct.fromTinkPayment(paymentRequest.getPayment()),
                        strongAuthenticationState.getState(),
                        requestBody);
        storage.storePaymentAuthorizationUrl(
                createPaymentResponse.getPaymentId(), createPaymentResponse.getScaRedirectLink());
        return createPaymentResponse.toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                URL authorizeUrl =
                        new URL(
                                storage.getPaymentAuthorizationUrl(
                                        paymentMultiStepRequest.getPayment().getUniqueId()));
                supplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            case PAYMENT_POST_SIGN_STATE:
                GetPaymentStatusResponse responseStatus =
                        apiClient.getPaymentStatus(
                                FinecoBankPaymentProduct.fromTinkPayment(payment),
                                payment.getUniqueId());
                payment.setStatus(
                        FinecoBankPaymentStatus.mapToTinkPaymentStatus(
                                FinecoBankPaymentStatus.fromString(responseStatus.getStatus())));

                // react to status?

                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.UNKNOWN_SIGNING_STEP,
                                paymentMultiStepRequest.getStep()));
        }
        return new PaymentMultiStepResponse(payment, nextStep, Collections.emptyList());
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
