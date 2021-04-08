package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import static java.util.Objects.nonNull;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CbiGlobePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final CbiGlobeApiClient apiClient;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Provider provider;

    private static final Logger logger = LoggerFactory.getLogger(CbiGlobePaymentExecutor.class);

    public CbiGlobePaymentExecutor(
            CbiGlobeApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState,
            Provider provider) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.provider = provider;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        fetchToken();

        sessionStorage.put(QueryKeys.STATE, strongAuthenticationState.getState());
        sessionStorage.put(
                CbiGlobeConstants.HeaderKeys.PSU_IP_ADDRESS, paymentRequest.getOriginatingUserIp());

        CreatePaymentRequest createPaymentRequest =
                PaymentServiceType.PERIODIC.equals(
                                paymentRequest.getPayment().getPaymentServiceType())
                        ? getCreateRecurringPaymentRequest(paymentRequest.getPayment())
                        : getCreatePaymentRequest(paymentRequest.getPayment());

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(createPaymentRequest, paymentRequest.getPayment());

        sessionStorage.put(
                StorageKeys.LINK,
                createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect().getHref());
        return createPaymentResponse.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    private CreatePaymentRequest getCreatePaymentRequest(Payment payment) {
        return CreatePaymentRequest.builder()
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .build();
    }

    private CreatePaymentRequest getCreateRecurringPaymentRequest(Payment payment) {

        return CreateRecurringPaymentRequest.builder()
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate().toString())
                // optional attributes
                .endDate(payment.getEndDate() != null ? payment.getEndDate().toString() : null)
                .executionRule(
                        payment.getExecutionRule() != null
                                ? payment.getExecutionRule().toString()
                                : null)
                .dayOfExecution(
                        nonNull(payment.getDayOfExecution())
                                ? String.valueOf(payment.getDayOfExecution())
                                : null)
                .build();
    }

    private RemittanceInformation getRemittanceInformation(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        return remittanceInformation;
    }

    private InstructedAmountEntity getInstructedAmountEntity(Payment payment) {
        return new InstructedAmountEntity(
                payment.getExactCurrencyAmount().getCurrencyCode(),
                String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()));
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }

    private void fetchToken() {
        try {
            if (!apiClient.isTokenValid()) {
                apiClient.getAndSaveToken();
            }
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (message.contains(MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name())) {
                apiClient.getAndSaveToken();
            } else {
                throw e;
            }
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment())
                .toTinkPaymentResponse(paymentRequest.getPayment());
    }

    private CreatePaymentResponse fetchPaymentStatus(Payment payment) {
        return apiClient.getPaymentStatus(payment);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentMultiStepResponse paymentMultiStepResponse;
        String redirectUrl = sessionStorage.get(StorageKeys.LINK);
        Map<String, String> supplementalInfo = null;
        if (redirectUrl != null) { // dont redirect if CBI globe dont provide redirect URL.
            openThirdPartyApp(new URL(redirectUrl));
            // after redirect is done remove old redirect link from session, because
            // if 5xx received from CBI Globe bank status polling then old redirect link is not
            // removed from session and TL again redirect to bank.
            sessionStorage.put(StorageKeys.LINK, null);

            supplementalInfo = waitForSupplementalInformation();
        }

        CreatePaymentResponse createPaymentResponse =
                fetchPaymentStatus(paymentMultiStepRequest.getPayment());

        CbiGlobePaymentStatus cbiGlobePaymentStatus =
                CbiGlobePaymentStatus.fromString(createPaymentResponse.getTransactionStatus());
        String scaStatus = createPaymentResponse.getScaStatus();
        String psuAuthenticationStatus = createPaymentResponse.getPsuAuthenticationStatus();

        switch (cbiGlobePaymentStatus) {
                // After signing PIS
            case ACCP:
            case ACSC:
            case ACSP:
            case ACTC:
            case ACWC:
            case ACWP:
                paymentMultiStepResponse =
                        handleSignedPayment(
                                paymentMultiStepRequest, createPaymentResponse, supplementalInfo);
                break;
                // Before signing PIS
            case RCVD:
            case PDNG:
                paymentMultiStepResponse =
                        handleUnsignedPayment(
                                paymentMultiStepRequest, createPaymentResponse, supplementalInfo);
                break;
            case RJCT: // cancelled case
                return handleReject(scaStatus, psuAuthenticationStatus);
            default:
                logger.error(
                        "Payment failed. Invalid Payment status returned by CBI Globe cbiGlobePaymentStatus={}",
                        cbiGlobePaymentStatus);
                throw new PaymentException("Payment failed");
        }

        // Note: this method should never return null, If this scenario happen then
        // CBI Globe might have changed Payment Status Codes
        return paymentMultiStepResponse;
    }

    private PaymentMultiStepResponse handleReject(String scaStatus, String psuAuthenticationStatus)
            throws PaymentAuthenticationException, PaymentRejectedException {
        if (CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATION_FAILED.equalsIgnoreCase(
                psuAuthenticationStatus)) {
            logger.error(
                    "PSU Authentication failed, psuAuthenticationStatus={}",
                    psuAuthenticationStatus);
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        } else {
            logger.error(
                    "Payment rejected by ASPSP: psuAuthenticationStatus={} , scaStatus={}",
                    psuAuthenticationStatus,
                    scaStatus);
            throw new PaymentRejectedException();
        }
    }

    private PaymentMultiStepResponse handleUnsignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse,
            Map<String, String> supplementalInfo)
            throws PaymentException {

        return createPaymentResponse.getLinks() == null
                ? handleEmptyLinksInResponse(
                        paymentMultiStepRequest, createPaymentResponse, supplementalInfo)
                : handleRedirectURLs(paymentMultiStepRequest, createPaymentResponse);
    }

    private PaymentMultiStepResponse handleEmptyLinksInResponse(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse,
            Map<String, String> supplementalInfo)
            throws PaymentException {
        sessionStorage.put(StorageKeys.LINK, null);
        // As for BPM payment is parked for 30 min at bank in RCVD state we need special handling.
        // Ref: https://tinkab.atlassian.net/browse/PAY2-734
        if (isBPMProvider()) {
            return handleSignedPayment(
                    paymentMultiStepRequest, createPaymentResponse, supplementalInfo);
        } else
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(
                            paymentMultiStepRequest.getPayment()),
                    CbiGlobeConstants.PaymentStep.IN_PROGRESS,
                    new ArrayList<>());
    }

    private PaymentMultiStepResponse handleRedirectURLs(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {
        String psuAuthenticationStatus = createPaymentResponse.getPsuAuthenticationStatus();
        if (CbiGlobeConstants.PSUAuthenticationStatus.IDENTIFICATION_REQUIRED.equalsIgnoreCase(
                        psuAuthenticationStatus)
                || CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATION_REQUIRED
                        .equalsIgnoreCase(psuAuthenticationStatus)) {
            sessionStorage.put(
                    StorageKeys.LINK,
                    createPaymentResponse
                            .getLinks()
                            .getUpdatePsuAuthenticationRedirect()
                            .getHref());

        } else if (createPaymentResponse.getLinks().getScaRedirect() != null) {
            sessionStorage.put(
                    StorageKeys.LINK, createPaymentResponse.getLinks().getScaRedirect().getHref());
        } else if (createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect() != null) {
            sessionStorage.put(
                    StorageKeys.LINK,
                    createPaymentResponse
                            .getLinks()
                            .getUpdatePsuAuthenticationRedirect()
                            .getHref());
        }
        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentMultiStepRequest.getPayment()),
                CbiGlobeConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse handleSignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse,
            Map<String, String> supplementalInfo)
            throws PaymentException {

        if (CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATED.equalsIgnoreCase(
                        createPaymentResponse.getPsuAuthenticationStatus())
                && CbiGlobeConstants.PSUAuthenticationStatus.VERIFIED.equalsIgnoreCase(
                        createPaymentResponse.getScaStatus())) {
            return getSuccessfulPaymentMultiStepResponse(
                    paymentMultiStepRequest, createPaymentResponse);
        } else if (CbiGlobeConstants.PSUAuthenticationStatus.FAILED.equalsIgnoreCase(
                createPaymentResponse.getScaStatus())) {
            return logAndThrowPaymentCancelledException(createPaymentResponse);
        } else if (supplementalInfo != null && supplementalInfo.isEmpty()) {
            return logAndThrowPaymentAuthorizationException(createPaymentResponse);
        } else {
            return handleIntermediatePaymentStates(paymentMultiStepRequest, createPaymentResponse);
        }
    }

    private PaymentMultiStepResponse getSuccessfulPaymentMultiStepResponse(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {
        if (isBPMProvider()) {
            paymentMultiStepRequest.getPayment().setStatus(PaymentStatus.SIGNED);
        }
        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentMultiStepRequest.getPayment()),
                AuthenticationStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse logAndThrowPaymentCancelledException(
            CreatePaymentResponse createPaymentResponse) throws PaymentCancelledException {
        logger.error(
                "Payment cancelled by user: psuAuthenticationStatus={} , scaStatus={}",
                createPaymentResponse.getPsuAuthenticationStatus(),
                createPaymentResponse.getScaStatus());
        throw new PaymentCancelledException();
    }

    private PaymentMultiStepResponse logAndThrowPaymentAuthorizationException(
            CreatePaymentResponse createPaymentResponse) throws PaymentAuthorizationException {
        logger.error(
                "Payment is not verified and we did not receive a callback from ASPSP: psuAuthenticationStatus={} , scaStatus={} , transactionStatus={}",
                createPaymentResponse.getPsuAuthenticationStatus(),
                createPaymentResponse.getScaStatus(),
                createPaymentResponse.getTransactionStatus());
        throw new PaymentAuthorizationException();
    }

    private PaymentMultiStepResponse handleIntermediatePaymentStates(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {

        String redirectURL = null;
        if (createPaymentResponse.getLinks() != null) {
            if (createPaymentResponse.getLinks().getScaRedirect() != null) {
                redirectURL = createPaymentResponse.getLinks().getScaRedirect().getHref();

            } else if (createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect()
                    != null) {
                redirectURL =
                        createPaymentResponse
                                .getLinks()
                                .getUpdatePsuAuthenticationRedirect()
                                .getHref();
            }

            // redirect URl from Bank should be null for intermediate states, If
            // not null then it may be bug on CBI globe
            if (redirectURL != null) {
                logger.warn("IntermediatePaymentStates redirectURl was NOT null, check logs");
            }
        }
        sessionStorage.put(
                StorageKeys.LINK,
                redirectURL); // redirectURL should be set to null to avoid multiple redirect to

        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentMultiStepRequest.getPayment()),
                CbiGlobeConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
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
        // Not implemented on banks side
        return new PaymentListResponse(paymentResponses);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private Map<String, String> waitForSupplementalInformation() {
        return this.supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9L, TimeUnit.MINUTES)
                .orElse(Collections.emptyMap());
    }

    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifierType, AccountIdentifierType> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    private boolean isBPMProvider() {
        return "it-bpm-oauth2".equalsIgnoreCase(provider.getName());
    }

    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
                                    genericBuilder()
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(
                                            AccountIdentifierType.IBAN, AccountIdentifierType.IBAN))
                            .build();
}
