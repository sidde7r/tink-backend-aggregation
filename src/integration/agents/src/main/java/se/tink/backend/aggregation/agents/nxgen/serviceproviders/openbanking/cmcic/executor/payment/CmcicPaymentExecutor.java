package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.DateFormat;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.PaymentSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ConfirmationResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ServiceLevelCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.StatusReasonInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity.AcceptedAuthenticationApproachEnum;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.uuid.UUIDUtils;

public class CmcicPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private CmcicApiClient apiClient;
    private SessionStorage sessionStorage;
    private String redirectUrl;
    private List<PaymentResponse> paymentResponses;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final Logger logger = LoggerFactory.getLogger(CmcicPaymentExecutor.class);

    public CmcicPaymentExecutor(
            CmcicApiClient apiClient,
            SessionStorage sessionStorage,
            AgentConfiguration<CmcicConfiguration> agentConfiguration,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        paymentResponses = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisOauthToken();
        sessionStorage.put(StorageKeys.STATE, strongAuthenticationState.getState());
        PaymentRequestResourceEntity paymentRequestResourceEntity =
                buildPaymentRequest(paymentRequest);
        HalPaymentRequestCreation paymentRequestCreation =
                apiClient.makePayment(paymentRequestResourceEntity);

        String authorizeUrl =
                Optional.ofNullable(
                                paymentRequestCreation.getLinks().getConsentApproval().getHref())
                        .orElseThrow(
                                () -> {
                                    logger.error(
                                            "Payment authorization failed. There is no authentication url!");
                                    return new PaymentAuthorizationException(
                                            TransferExecutionException.EndUserMessage
                                                    .PAYMENT_AUTHORIZATION_FAILED
                                                    .getKey()
                                                    .get(),
                                            new PaymentRejectedException());
                                });

        sessionStorage.put(StorageKeys.AUTH_URL, authorizeUrl);

        PaymentResponse res = getPaymentResponse(paymentRequestResourceEntity);

        paymentResponses.add(res);

        return res;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        HalPaymentRequestEntity paymentRequestEntity =
                apiClient.fetchPayment(paymentRequest.getPayment().getUniqueId());

        PaymentResponseEntity payment = paymentRequestEntity.getPaymentRequest();

        return getPaymentResponse(payment);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        Payment payment = paymentMultiStepRequest.getPayment();
        String paymentId = getPaymentId(sessionStorage.get(StorageKeys.AUTH_URL));
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                openThirdPartyApp(new URL(sessionStorage.get(StorageKeys.AUTH_URL)));
                waitForSupplementalInformation();
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(
                                payment, PaymentSteps.POST_SIGN_STEP, new ArrayList<>());
                break;
            case PaymentSteps.POST_SIGN_STEP:
                HalPaymentRequestEntity getPaymentResponse = apiClient.fetchPayment(paymentId);
                paymentMultiStepResponse =
                        handleSignedPayment(getPaymentResponse, PaymentSteps.CONFIRM_PAYMENT_STEP);
                break;
            case PaymentSteps.CONFIRM_PAYMENT_STEP:
                ConfirmationResourceEntity confirmationResourceEntity =
                        new ConfirmationResourceEntity();
                confirmationResourceEntity.setPsuAuthenticationFactor(
                        sessionStorage.get(StorageKeys.AUTH_FACTOR));
                HalPaymentRequestEntity paymentConfirmResponse =
                        apiClient.confirmPayment(paymentId, confirmationResourceEntity);
                paymentMultiStepResponse =
                        handleSignedPayment(
                                paymentConfirmResponse, AuthenticationStepConstants.STEP_FINALIZE);
                break;
            default:
                logger.error(
                        "Payment failed due to unknown sign step{} ",
                        paymentMultiStepRequest.getStep());
                throw new PaymentException(
                        TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                .getKey()
                                .get());
        }
        return paymentMultiStepResponse;
    }

    private PaymentMultiStepResponse handleSignedPayment(
            HalPaymentRequestEntity paymentResponse, String nextStep) throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentInformationStatusCodeEntity paymentStatus =
                paymentResponse.getPaymentRequest().getPaymentInformationStatusCode();
        switch (paymentStatus) {
            case ACCP:
                if (nextStep.equals(AuthenticationStepConstants.STEP_FINALIZE)) {
                    logger.error(
                            "Payment confirmation failed, psuAuthenticationStatus={}",
                            paymentStatus);
                    throw new PaymentAuthenticationException(
                            TransferExecutionException.EndUserMessage.PAYMENT_CONFIRMATION_FAILED
                                    .getKey()
                                    .get(),
                            new PaymentRejectedException());
                } else {
                    paymentMultiStepResponse =
                            new PaymentMultiStepResponse(
                                    getPaymentResponse(paymentResponse.getPaymentRequest()),
                                    nextStep,
                                    new ArrayList<>());
                }
                break;
            case PDNG:
            case ACSC:
            case ACSP:
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(
                                getPaymentResponse(paymentResponse.getPaymentRequest()),
                                nextStep,
                                new ArrayList<>());
                break;
            case ACTC:
            case ACWC:
            case ACWP:
            case PART:
            case RCVD:
                logger.error(
                        "PSU Authentication failed, psuAuthenticationStatus={}", paymentStatus);
                throw new PaymentAuthenticationException(
                        TransferExecutionException.EndUserMessage.PAYMENT_AUTHENTICATION_FAILED
                                .getKey()
                                .get(),
                        new PaymentRejectedException());
            case RJCT:
                handleReject(paymentResponse.getPaymentRequest().getStatusReasonInformation());
                break;
            case CANC:
                handleCancel(paymentResponse.getPaymentRequest().getStatusReasonInformation());
                break;
            default:
                logger.error(
                        "Payment failed. Invalid Payment status returned by Societe Generale Bank,Status={}",
                        paymentStatus);
                throw new PaymentException(
                        TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                .getKey()
                                .get());
        }
        return paymentMultiStepResponse;
    }

    private void handleReject(StatusReasonInformationEntity rejectStatus)
            throws PaymentRejectedException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Payment Rejected by the bank with error ={}", error);
        throw new PaymentRejectedException(
                TransferExecutionException.EndUserMessage.PAYMENT_REJECTED.getKey().get());
    }

    private void handleCancel(StatusReasonInformationEntity rejectStatus)
            throws PaymentAuthenticationException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Authorisation of payment was cancelled with bank status={}", error);
        throw new PaymentAuthenticationException(
                TransferExecutionException.EndUserMessage.PAYMENT_CANCELLED.getKey().get(),
                new PaymentCancelledException());
    }

    private PaymentResponse getPaymentResponse(PaymentResponseEntity payment) {

        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getResourceId())
                        .withStatus(payment.getPaymentInformationStatusCode().getPaymentStatus())
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                payment.getBeneficiary()
                                                        .getCreditorAccount()
                                                        .getIban()),
                                        payment.getBeneficiary().getCreditor().getName()))
                        .build());
    }

    private PaymentResponse getPaymentResponse(PaymentRequestResourceEntity payment) {
        AmountTypeEntity amountTypeEntity =
                payment.getCreditTransferTransaction().get(0).getInstructedAmount();
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getResourceId())
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        amountTypeEntity.getAmount(),
                                        amountTypeEntity.getCurrency()))
                        .withStatus(PaymentStatus.PENDING)
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                payment.getBeneficiary()
                                                        .getCreditorAccount()
                                                        .getIban()),
                                        payment.getBeneficiary().getCreditor().getName()))
                        .withDebtor(
                                new Debtor(
                                        Optional.ofNullable(payment.getDebtorAccount())
                                                .map(
                                                        accountIdentificationEntity ->
                                                                new IbanIdentifier(
                                                                        accountIdentificationEntity
                                                                                .getIban()))
                                                .orElse(null)))
                        .withExecutionDate(
                                parseDate(payment.getRequestedExecutionDate()).toLocalDate())
                        .build());
    }

    private LocalDateTime parseDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(DateFormat.DATE_FORMAT);
            return LocalDateTime.ofInstant(format.parse(date).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            return OffsetDateTime.parse(date).toLocalDateTime();
        }
    }

    private String getPaymentId(String authorizationUrl) throws PaymentException {
        int index = authorizationUrl.lastIndexOf('=');
        if (index < 0) {
            logger.error("Payment failed due to missing paymentId");
            throw new PaymentException(
                    TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                            .getKey()
                            .get());
        }
        return authorizationUrl.substring(index + 1);
    }

    private PaymentRequestResourceEntity buildPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        PartyIdentificationEntity initiatingParty =
                new PartyIdentificationEntity(FormValues.CREDITOR_NAME, null, null, null);

        PaymentTypeInformationEntity paymentTypeInformation =
                new PaymentTypeInformationEntity(null, ServiceLevelCodeEntity.SEPA, null, null);

        AccountIdentificationEntity debtorAccount =
                Optional.ofNullable(paymentRequest.getPayment().getDebtor())
                        .map(
                                debtor ->
                                        new AccountIdentificationEntity(
                                                debtor.getAccountNumber(), null))
                        .orElse(null);

        PartyIdentificationEntity creditor =
                new PartyIdentificationEntity(
                        getPresetOrDefaultCreditorName(paymentRequest), null, null, null);

        AccountIdentificationEntity creditorAccount =
                new AccountIdentificationEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(), null);

        BeneficiaryEntity beneficiary =
                BeneficiaryEntity.builder()
                        .creditor(creditor)
                        .creditorAccount(creditorAccount)
                        .build();

        PaymentIdentificationEntity paymentId =
                new PaymentIdentificationEntity(
                        payment.getUniqueId(), UUIDUtils.generateUUID(), null);

        AmountTypeEntity instructedAmount =
                new AmountTypeEntity(
                        payment.getExactCurrencyAmount().getCurrencyCode(),
                        payment.getExactCurrencyAmount().getExactValue().toString());

        RemittanceInformationEntity remittanceInformation = new RemittanceInformationEntity();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                payment.getRemittanceInformation(), null, RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setUnstructured(
                Collections.singletonList(payment.getRemittanceInformation().getValue()));

        List<CreditTransferTransactionEntity> creditTransferTransaction =
                Collections.singletonList(
                        CreditTransferTransactionEntity.builder()
                                .paymentId(paymentId)
                                .instructedAmount(instructedAmount)
                                .remittanceInformation(remittanceInformation)
                                .build());

        String callbackUrl =
                redirectUrl + Urls.SUCCESS_REPORT_PATH + sessionStorage.get(StorageKeys.STATE);

        List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach =
                Collections.singletonList(AcceptedAuthenticationApproachEnum.REDIRECT);
        SupplementaryDataEntity supplementaryData =
                SupplementaryDataEntity.builder()
                        .acceptedAuthenticationApproach(acceptedAuthenticationApproach)
                        .successfulReportUrl(callbackUrl)
                        .unsuccessfulReportUrl(callbackUrl)
                        .build();

        return PaymentRequestResourceEntity.builder()
                .paymentInformationId(UUIDUtils.generateUUID())
                .creationDateTime(OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                .numberOfTransactions(FormValues.NUMBER_OF_TRANSACTIONS)
                .requestedExecutionDate(getExecutionDate(payment.getExecutionDate()))
                .initiatingParty(initiatingParty)
                .paymentTypeInformation(paymentTypeInformation)
                .debtorAccount(debtorAccount)
                .beneficiary(beneficiary)
                .creditTransferTransaction(creditTransferTransaction)
                .supplementaryData(supplementaryData)
                .build();
    }

    private String getExecutionDate(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(
                        date ->
                                localDate
                                        .atStartOfDay()
                                        .atZone(ZoneId.of("CET"))
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(
                        LocalDateTime.now()
                                .atZone(ZoneId.of("CET"))
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() throws PaymentAuthenticationException {
        Map<String, String> callbackData =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                9L,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                () ->
                                        new PaymentAuthenticationException(
                                                "Payment authentication failed. There is no authorization url!",
                                                new PaymentRejectedException()));

        // Query parameters can be case insesitive returned by bank,this is to take care of that
        // situation and we avoid failing the payment.
        Map<String, String> caseInsensitiveCallbackData = new CaseInsensitiveMap<>(callbackData);

        String psuAuthenticationFactor =
                caseInsensitiveCallbackData.get(CmcicConstants.QueryKeys.PSU_AUTHENTICATION_FACTOR);
        if (Strings.isNullOrEmpty(psuAuthenticationFactor)) {
            handelAuthFactorError();
        }
        sessionStorage.put(StorageKeys.AUTH_FACTOR, psuAuthenticationFactor);
    }

    private String getPresetOrDefaultCreditorName(PaymentRequest paymentRequest) {
        String creditorName = paymentRequest.getPayment().getCreditor().getName();
        return Strings.isNullOrEmpty(creditorName) ? FormValues.BENEFICIARY_NAME : creditorName;
    }

    private void handelAuthFactorError() throws PaymentAuthenticationException {
        logger.error("Payment authorization failed. There is no psuAuthenticationFactor!");
        throw new PaymentAuthenticationException(
                TransferExecutionException.EndUserMessage.PAYMENT_AUTHENTICATION_FAILED
                        .getKey()
                        .get(),
                new PaymentRejectedException());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(paymentResponses);
    }
}
