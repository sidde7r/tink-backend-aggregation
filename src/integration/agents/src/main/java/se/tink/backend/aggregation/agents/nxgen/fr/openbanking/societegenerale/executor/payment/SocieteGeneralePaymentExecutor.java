package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.CONFIRM_PAYMENT_STEP;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentTypeInformation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.ConsentApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentRequestLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SocieteGeneraleDateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.CreatePaymentRequestValidator;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
@RequiredArgsConstructor
public class SocieteGeneralePaymentExecutor implements PaymentExecutor {
    private final SocieteGeneraleApiClient apiClient;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CreatePaymentRequestValidator createPaymentRequestValidator;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisAccessToken();
        sessionStorage.put(
                SocieteGeneraleConstants.QueryKeys.STATE, strongAuthenticationState.getState());
        PaymentType type = PaymentType.SEPA;
        CreatePaymentRequest request = buildPaymentRequest(paymentRequest);
        createPaymentRequestValidator.validate(request);
        CreatePaymentResponse paymentResponse = apiClient.createPayment(request);
        String authorizeUrl =
                Optional.ofNullable(paymentResponse)
                        .map(CreatePaymentResponse::getLinks)
                        .map(PaymentRequestLinkEntity::getConsentApproval)
                        .map(ConsentApprovalEntity::getUrl)
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "Payment authorization failed. There is no authentication url!");
                                    return new PaymentException(
                                            InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
                                });

        sessionStorage.put(SocieteGeneraleConstants.StorageKeys.AUTH_URL, authorizeUrl);

        return paymentResponse.toTinkPaymentResponse(type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse;
        Payment payment = paymentMultiStepRequest.getPayment();
        String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                openThirdPartyApp(
                        new URL(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.AUTH_URL)));
                waitForSupplementalInformation();
                paymentMultiStepResponse = new PaymentMultiStepResponse(payment, POST_SIGN_STEP);
                break;
            case POST_SIGN_STEP:
                GetPaymentResponse getPaymentResponse = fetchPaymentStatus(paymentId);
                paymentMultiStepResponse =
                        handleSignedPayment(getPaymentResponse, CONFIRM_PAYMENT_STEP);
                break;
            case CONFIRM_PAYMENT_STEP:
                GetPaymentResponse paymentConfirmationResponse =
                        apiClient.confirmPayment(paymentId);
                paymentMultiStepResponse =
                        handleSignedPayment(
                                paymentConfirmationResponse,
                                AuthenticationStepConstants.STEP_FINALIZE);
                break;
            default:
                log.error(
                        "Payment failed due to unknown sign step{} ",
                        paymentMultiStepRequest.getStep());
                throw new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return paymentMultiStepResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    private PaymentMultiStepResponse handleSignedPayment(
            GetPaymentResponse paymentResponse, String nextStep) throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentInformationStatusCodeEntity societeGeneralePaymentStatus =
                paymentResponse.getPayment().getPaymentInformationStatus();
        switch (societeGeneralePaymentStatus) {
            case ACCP:
                if (nextStep.equals(AuthenticationStepConstants.STEP_FINALIZE)) {
                    log.error(
                            "Payment confirmation failed, psuAuthenticationStatus={}",
                            societeGeneralePaymentStatus);
                    throw new PaymentAuthenticationException(
                            InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
                }
            case PDNG:
            case ACSC:
            case ACSP:
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(
                                paymentResponse.toTinkPaymentResponse(), nextStep);
                break;
            case ACTC:
            case ACWC:
            case ACWP:
            case PART:
            case RCVD:
                log.error(
                        "PSU Authentication failed, psuAuthenticationStatus={}",
                        societeGeneralePaymentStatus);
                throw new PaymentAuthenticationException(
                        InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
            case RJCT:
            case CANC:
                paymentResponse.getPayment().getStatusReasonInformation().mapToError();
                break;
            default:
                log.error(
                        "Payment failed. Invalid Payment status returned by Societe Generale Bank,Status={}",
                        societeGeneralePaymentStatus);
                throw new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return paymentMultiStepResponse;
    }

    private CreatePaymentRequest buildPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        BeneficiaryEntity beneficiary = BeneficiaryEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> creditTransferTransaction =
                CreditTransferTransactionEntity.of(paymentRequest);
        SupplementaryDataEntity supplementaryData =
                SupplementaryDataEntity.of(paymentRequest, createRedirectUrl().toString());

        return new CreatePaymentRequest.Builder()
                .withPaymentInformationId(UUID.randomUUID().toString())
                .withCreationDateTime(SocieteGeneraleDateUtil.getCreationDate())
                .withNumberOfTransactions(
                        SocieteGeneraleConstants.FormValues.NUMBER_OF_TRANSACTIONS)
                .withInitiatingParty(
                        new PartyIdentificationEntity(
                                SocieteGeneraleConstants.FormValues
                                        .PAYMENT_INITIATION_DEFAULT_NAME))
                .withPaymentTypeInformation(
                        new PaymentTypeInformationEntity(
                                SocieteGeneraleConstants.PaymentTypeInformation.SERVICE_LEVEL,
                                PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER
                                                == payment.getPaymentScheme()
                                        ? PaymentTypeInformation.SEPA_INSTANT_CREDIT_TRANSFER
                                        : null))
                .withDebtorAccount(
                        new DebtorAccountEntity(
                                Optional.ofNullable(payment.getDebtor())
                                        .map(Debtor::getAccountNumber)
                                        .orElse(null)))
                .withBeneficiary(beneficiary)
                .withChargeBearer(SocieteGeneraleConstants.FormValues.CHARGE_BEARER_SLEV)
                .withRequestedExecutionDate(
                        SocieteGeneraleDateUtil.getExecutionDate(payment.getExecutionDate()))
                .withCreditTransferTransaction(creditTransferTransaction)
                .withSupplementaryData(supplementaryData)
                .build();
    }

    private GetPaymentResponse fetchPaymentStatus(String paymentId) {
        return apiClient.getPaymentStatus(paymentId);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() throws PaymentAuthorizationException {
        this.supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9L, TimeUnit.MINUTES)
                .orElseThrow(PaymentAuthorizationTimeOutException::new);
    }

    private URL createRedirectUrl() {
        return new URL(redirectUrl)
                .queryParam(
                        SocieteGeneraleConstants.QueryKeys.STATE,
                        strongAuthenticationState.getState())
                .queryParam(
                        SocieteGeneraleConstants.QueryKeys.CODE,
                        SocieteGeneraleConstants.QueryValues.CODE);
    }
}
