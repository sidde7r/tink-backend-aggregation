package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.FormValues.DEFAULT_ZONE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.CONFIRM_PAYMENT_STEP;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.StatusReasonInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.GetPaymentResponse;
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
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class SocieteGeneralePaymentExecutor implements PaymentExecutor {
    private final SocieteGeneraleApiClient apiClient;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final Logger logger =
            LoggerFactory.getLogger(SocieteGeneralePaymentExecutor.class);

    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN))
                            .build();

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisAccessToken();
        sessionStorage.put(
                SocieteGeneraleConstants.QueryKeys.STATE, strongAuthenticationState.getState());
        PaymentType type = getPaymentType(paymentRequest);
        CreatePaymentRequest request = buildPaymentRequest(paymentRequest);
        CreatePaymentResponse paymentResponse = apiClient.createPayment(request);
        String authorizeUrl =
                Optional.ofNullable(paymentResponse.getLinks().getConsentApproval().getHref())
                        .orElseThrow(
                                () ->
                                        new PaymentAuthenticationException(
                                                "Payment authentication failed. There is no authorization url!",
                                                new PaymentRejectedException()));

        sessionStorage.put(SocieteGeneraleConstants.StorageKeys.AUTORIZE_URL, authorizeUrl);

        return paymentResponse.toTinkPaymentResponse(type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        Payment payment = paymentMultiStepRequest.getPayment();
        String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                openThirdPartyApp(
                        new URL(
                                sessionStorage.get(
                                        SocieteGeneraleConstants.StorageKeys.AUTORIZE_URL)));
                waitForSupplementalInformation();
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(payment, POST_SIGN_STEP, new ArrayList<>());
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
                throw new PaymentException(
                        "Payment failed due to unknown sign step "
                                + paymentMultiStepRequest.getStep());
        }
        return paymentMultiStepResponse;
    }

    private PaymentMultiStepResponse handleSignedPayment(
            GetPaymentResponse paymentResponse, String nextStep) throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentInformationStatusCodeEntity societeGeneralePaymentStatus =
                paymentResponse.getPayment().getPaymentInformationStatus();
        switch (societeGeneralePaymentStatus) {
            case ACCP:
                if (nextStep.equals(AuthenticationStepConstants.STEP_FINALIZE)) {
                    logger.error(
                            "Payment confirmation failed, psuAuthenticationStatus={}",
                            societeGeneralePaymentStatus);
                    throw new PaymentAuthenticationException(
                            "Payment confirmation failed.", new PaymentRejectedException());
                }
            case PDNG:
            case ACSC:
            case ACSP:
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(
                                paymentResponse.toTinkPaymentResponse(),
                                nextStep,
                                new ArrayList<>());
                break;
            case ACTC:
            case ACWC:
            case ACWP:
            case PART:
            case RCVD:
                logger.error(
                        "PSU Authentication failed, psuAuthenticationStatus={}",
                        societeGeneralePaymentStatus);
                throw new PaymentAuthenticationException(
                        "Payment authentication failed.", new PaymentRejectedException());
            case RJCT:
                handleReject(paymentResponse.getPayment().getStatusReasonInformation());
                break;
            case CANC:
                handleCancelled(paymentResponse.getPayment().getStatusReasonInformation());
                break;
            default:
                logger.error(
                        "Payment failed. Invalid Payment status returned by Societe Generale Bank,Status={}",
                        societeGeneralePaymentStatus);
                throw new PaymentException("Payment failed");
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

    private CreatePaymentRequest buildPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        BeneficiaryEntity beneficiary = BeneficiaryEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> creditTransferTransaction =
                CreditTransferTransactionEntity.of(paymentRequest);
        SupplementaryDataEntity supplementaryData =
                SupplementaryDataEntity.of(paymentRequest, createRedirectUrl().toString());

        return new CreatePaymentRequest.Builder()
                .withPaymentInformationId(UUID.randomUUID().toString())
                .withCreationDateTime(getCreationDate())
                .withNumberOfTransactions(
                        SocieteGeneraleConstants.FormValues.NUMBER_OF_TRANSACTIONS)
                .withInitiatingParty(
                        new PartyIdentificationEntity(
                                SocieteGeneraleConstants.FormValues
                                        .PAYMENT_INITIATION_DEFAULT_NAME))
                .withPaymentTypeInformation(
                        new PaymentTypeInformationEntity(
                                SocieteGeneraleConstants.PaymentTypeInformation.SERVICE_LEVEL))
                .withDebtorAccount(new DebtorAccountEntity(payment.getDebtor().getAccountNumber()))
                .withBeneficiary(beneficiary)
                .withChargeBearer(SocieteGeneraleConstants.FormValues.CHARGE_BEARER_SLEV)
                .withRequestedExecutionDate(getExecutionDate(payment.getExecutionDate()))
                .withCreditTransferTransaction(creditTransferTransaction)
                .withSupplementaryData(supplementaryData)
                .build();
    }

    private void handleReject(StatusReasonInformationEntity rejectStatus)
            throws PaymentRejectedException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Payment Rejected by the bank with error ={}", error);
        throw new PaymentRejectedException();
    }

    private void handleCancelled(StatusReasonInformationEntity rejectStatus)
            throws PaymentAuthorizationCancelledByUserException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Authorisation of payment was cancelled with bank status={}", error);
        throw new PaymentAuthorizationCancelledByUserException();
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

    private void waitForSupplementalInformation() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), 9L, TimeUnit.MINUTES);
    }

    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
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

    private String getCreationDate() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String getExecutionDate(LocalDate localDate) {
        // excexutionDate should not be same as creationDate due to bank limitation,so adding 1
        // minute will resolve this issue.
        return Optional.ofNullable(localDate)
                .map(
                        date ->
                                ZonedDateTime.of(
                                                date,
                                                LocalTime.now(DEFAULT_ZONE_ID),
                                                DEFAULT_ZONE_ID)
                                        .plusMinutes(1)
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(
                        ZonedDateTime.now(DEFAULT_ZONE_ID)
                                .plusMinutes(1)
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
