package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RequiredArgsConstructor
@Slf4j
public class CbiGlobePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    protected final CbiGlobeAuthApiClient authApiClient;
    protected final CbiGlobePaymentApiClient paymentApiClient;
    private List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Provider provider;
    private final CbiStorage storage;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreatePaymentRequest createPaymentRequest =
                PaymentServiceType.PERIODIC.equals(
                                paymentRequest.getPayment().getPaymentServiceType())
                        ? getCreateRecurringPaymentRequest(paymentRequest.getPayment())
                        : getCreatePaymentRequest(paymentRequest.getPayment());

        CreatePaymentResponse createPaymentResponse =
                paymentApiClient.createPayment(createPaymentRequest, paymentRequest.getPayment());
        authorizePayment(createPaymentResponse);
        return createPaymentResponse.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    protected void authorizePayment(CreatePaymentResponse createPaymentResponse) {
        storage.saveScaLinkForPayments(
                createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect().getHref());
    }

    private CreatePaymentRequest getCreatePaymentRequest(Payment payment) {
        return CreatePaymentRequest.builder()
                .debtorAccount(
                        getAccountEntity(
                                payment.getDebtor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(
                        getAccountEntity(
                                payment.getCreditor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .build();
    }

    private CreatePaymentRequest getCreateRecurringPaymentRequest(Payment payment) {

        return CreateRecurringPaymentRequest.builder()
                .debtorAccount(
                        getAccountEntity(
                                payment.getDebtor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(
                        getAccountEntity(
                                payment.getCreditor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate().toString())
                // optional attributes
                .endDate(payment.getEndDate() != null ? payment.getEndDate().toString() : null)
                .executionRule(
                        payment.getExecutionRule() != null
                                ? mapExecutionRule(payment.getExecutionRule())
                                : null)
                .dayOfExecution(getDayOfExecution(payment))
                .build();
    }

    private String mapExecutionRule(ExecutionRule rule) {
        // Bank API has a typo, we need to have a typo as well.
        if (rule == ExecutionRule.PRECEDING) {
            return "preceeding";
        } else {
            return rule.toString();
        }
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

    private String getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return String.valueOf(payment.getDayOfWeek().getValue());
            case MONTHLY:
                return payment.getDayOfMonth() != null
                        ? payment.getDayOfMonth().toString()
                        : null; // Credem hates this parameter
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return paymentApiClient
                .getPayment(paymentRequest.getPayment())
                .toTinkPaymentResponse(paymentRequest.getPayment());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {

        PaymentMultiStepResponse paymentMultiStepResponse;
        CreatePaymentResponse createPaymentResponse = fetchPaymentStatus(paymentMultiStepRequest);
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
                        handleSignedPayment(paymentMultiStepRequest, createPaymentResponse);
                break;
                // Before signing PIS
            case RCVD:
            case PDNG:
                paymentMultiStepResponse =
                        handleUnsignedPayment(paymentMultiStepRequest, createPaymentResponse);
                break;
            case RJCT: // cancelled case
                return handleReject(scaStatus, psuAuthenticationStatus);
            default:
                log.error(
                        "Payment failed. Invalid Payment status returned by CBI Globe cbiGlobePaymentStatus={}",
                        cbiGlobePaymentStatus);
                throw new PaymentException("Payment failed");
        }

        // Note: this method should never return null, If this scenario happen then
        // CBI Globe might have changed Payment Status Codes
        return paymentMultiStepResponse;
    }

    private CreatePaymentResponse fetchPaymentStatus(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException {
        CreatePaymentResponse createPaymentResponse;
        String redirectUrl = storage.getScaLinkForPayments();
        // We observed that in case of sepa payments fetching payment status for the 1st time
        // doesn't give us redirectUrl, so we need to repeat this operation. There is no clear
        // explanation from CBI. It usually doesn't happen for instant payments
        if (redirectUrl == null) {
            log.info("No redirect, fetching payment status to get redirect link");
            createPaymentResponse = fetchPaymentStatusOrThrowException(paymentMultiStepRequest);
        } else {
            log.info("Redirect present, fetching payment status depending on supplementalInfo");
            Map<String, String> supplementalInfo = fetchSupplementalInfo(redirectUrl);
            if (QueryValues.SUCCESS.equals(supplementalInfo.get(QueryKeys.RESULT))) {
                createPaymentResponse = fetchPaymentStatusOrThrowException(paymentMultiStepRequest);
            } else {
                throw new PaymentAuthorizationException();
            }
        }
        return createPaymentResponse;
    }

    protected Map<String, String> fetchSupplementalInfo(String redirectUrl) {
        if (redirectUrl != null) {
            openThirdPartyApp(new URL(redirectUrl));
            // after redirect is done remove old redirect link from session, because
            // if 5xx received from CBI Globe bank status polling then old redirect link is not
            // removed from session and TL again redirect to bank.
            storage.clearScaLinkForPayments();
            return waitForSupplementalInformation();
        } else {
            throw new PaymentAuthorizationException();
        }
    }

    private CreatePaymentResponse fetchPaymentStatusOrThrowException(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        CreatePaymentResponse createPaymentResponse;
        try {
            createPaymentResponse =
                    paymentApiClient.getPaymentStatus(paymentMultiStepRequest.getPayment());
        } catch (HttpResponseException httpResponseException) {
            ErrorResponse errorResponse =
                    ErrorResponse.createFrom(httpResponseException.getResponse());
            if (errorResponse != null
                    && (errorResponse.errorManagementDescriptionEquals(
                                    "Operation not allowed: authentication required.")
                            || errorResponse.tppMessagesContainsError(
                                    "GENERIC_ERROR", "Unknown Payment Identifier"))) {
                throw new PaymentAuthorizationCancelledByUserException();
            } else {
                throw httpResponseException;
            }
        }
        return createPaymentResponse;
    }

    private PaymentMultiStepResponse handleReject(String scaStatus, String psuAuthenticationStatus)
            throws PaymentAuthenticationException, PaymentRejectedException {
        if (CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATION_FAILED.equalsIgnoreCase(
                psuAuthenticationStatus)) {
            log.error(
                    "PSU Authentication failed, psuAuthenticationStatus={}",
                    psuAuthenticationStatus);
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        } else {
            log.error(
                    "Payment rejected by ASPSP: psuAuthenticationStatus={} , scaStatus={}",
                    psuAuthenticationStatus,
                    scaStatus);
            throw new PaymentRejectedException();
        }
    }

    private PaymentMultiStepResponse handleUnsignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse)
            throws PaymentException {

        return createPaymentResponse.getLinks() == null
                ? handleEmptyLinksInResponse(paymentMultiStepRequest, createPaymentResponse)
                : handleRedirectURLs(paymentMultiStepRequest, createPaymentResponse);
    }

    private PaymentMultiStepResponse handleEmptyLinksInResponse(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse)
            throws PaymentException {
        storage.clearScaLinkForPayments();
        // As for BPM payment is parked for 30 min at bank in RCVD state we need special handling.
        // Ref: https://tinkab.atlassian.net/browse/PAY2-734
        if (isBPMProvider()) {
            return handleSignedPayment(paymentMultiStepRequest, createPaymentResponse);
        } else
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(
                            paymentMultiStepRequest.getPayment()),
                    CbiGlobeConstants.PaymentStep.IN_PROGRESS);
    }

    private PaymentMultiStepResponse handleRedirectURLs(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {
        String psuAuthenticationStatus = createPaymentResponse.getPsuAuthenticationStatus();
        if (CbiGlobeConstants.PSUAuthenticationStatus.IDENTIFICATION_REQUIRED.equalsIgnoreCase(
                        psuAuthenticationStatus)
                || CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATION_REQUIRED
                        .equalsIgnoreCase(psuAuthenticationStatus)) {
            storage.saveScaLinkForPayments(
                    createPaymentResponse
                            .getLinks()
                            .getUpdatePsuAuthenticationRedirect()
                            .getHref());

        } else if (createPaymentResponse.getLinks().getScaRedirect() != null) {
            storage.saveScaLinkForPayments(
                    createPaymentResponse.getLinks().getScaRedirect().getHref());
        } else if (createPaymentResponse.getLinks().getUpdatePsuAuthenticationRedirect() != null) {
            storage.saveScaLinkForPayments(
                    createPaymentResponse
                            .getLinks()
                            .getUpdatePsuAuthenticationRedirect()
                            .getHref());
        }
        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentMultiStepRequest.getPayment()),
                CbiGlobeConstants.PaymentStep.IN_PROGRESS);
    }

    private PaymentMultiStepResponse handleSignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse)
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
                AuthenticationStepConstants.STEP_FINALIZE);
    }

    private PaymentMultiStepResponse logAndThrowPaymentCancelledException(
            CreatePaymentResponse createPaymentResponse) throws PaymentCancelledException {
        log.error(
                "Payment cancelled by user: psuAuthenticationStatus={} , scaStatus={}",
                createPaymentResponse.getPsuAuthenticationStatus(),
                createPaymentResponse.getScaStatus());
        throw new PaymentCancelledException();
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
                log.warn("IntermediatePaymentStates redirectURl was NOT null, check logs");
            }
        }
        storage.saveScaLinkForPayments(
                redirectURL); // redirectURL should be set to null to avoid multiple redirect to

        return new PaymentMultiStepResponse(
                createPaymentResponse.toTinkPaymentResponse(paymentMultiStepRequest.getPayment()),
                CbiGlobeConstants.PaymentStep.IN_PROGRESS);
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
                .orElseThrow(PaymentAuthorizationTimeOutException::new);
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
