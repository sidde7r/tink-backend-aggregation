package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
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
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
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

        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);
        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withDebtorAccount(debtorEntity)
                        .withInstructedAmount(instructedAmountEntity)
                        .withCreditorAccount(creditorEntity)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withRemittanceInformationUnstructured(remittanceInformation.getValue())
                        .withTransactionType(FormValues.TRANSACTION_TYPE)
                        .build();

        CreatePaymentResponse payment = apiClient.createPayment(createPaymentRequest);
        sessionStorage.put(
                StorageKeys.LINK,
                payment.getLinks().getUpdatePsuAuthenticationRedirect().getHref());
        return payment.toTinkPaymentResponse(getPaymentType(paymentRequest));
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
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment().getType());
    }

    private CreatePaymentResponse fetchPaymentStatus(String paymentId) {
        return apiClient.getPaymentStatus(paymentId);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentMultiStepResponse paymentMultiStepResponse = null;
        String redirectUrl = sessionStorage.get(StorageKeys.LINK);
        if (redirectUrl != null) { // dont redirect if CBI globe dont provide redirect URL.
            openThirdPartyApp(new URL(redirectUrl));
            waitForSupplementalInformation();
        }

        CreatePaymentResponse createPaymentResponse =
                fetchPaymentStatus(paymentMultiStepRequest.getPayment().getUniqueId());
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
            CreatePaymentResponse createPaymentResponse) {

        return createPaymentResponse.getLinks() == null
                ? handleEmptyLinksInResponse(paymentMultiStepRequest, createPaymentResponse)
                : handleRedirectURLs(paymentMultiStepRequest, createPaymentResponse);
    }

    private PaymentMultiStepResponse handleEmptyLinksInResponse(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {
        sessionStorage.put(StorageKeys.LINK, null);
        // As for BPM payment is parked for 30 min at bank in RCVD state we need special handling.
        // Ref: https://tinkab.atlassian.net/browse/PAY2-734
        if (isBPMProvider()) {
            return handleSignedPayment(paymentMultiStepRequest, createPaymentResponse);
        } else
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(
                            paymentMultiStepRequest.getPayment().getUniqueId(),
                            paymentMultiStepRequest.getPayment().getType()),
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
                createPaymentResponse.toTinkPaymentResponse(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        paymentMultiStepRequest.getPayment().getType()),
                CbiGlobeConstants.PaymentStep.IN_PROGRESS,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse handleSignedPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse) {

        if (CbiGlobeConstants.PSUAuthenticationStatus.AUTHENTICATED.equalsIgnoreCase(
                        createPaymentResponse.getPsuAuthenticationStatus())
                && CbiGlobeConstants.PSUAuthenticationStatus.VERIFIED.equalsIgnoreCase(
                        createPaymentResponse.getScaStatus())) {
            if (isBPMProvider()) {
                paymentMultiStepRequest.getPayment().setStatus(PaymentStatus.SIGNED);
            }
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(
                            paymentMultiStepRequest.getPayment().getType()),
                    AuthenticationStepConstants.STEP_FINALIZE,
                    new ArrayList<>());
        } else {
            return handleIntermediatePaymentStates(paymentMultiStepRequest, createPaymentResponse);
        }
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
                createPaymentResponse.toTinkPaymentResponse(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        paymentMultiStepRequest.getPayment().getType()),
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

    private boolean isBPMProvider() {
        return "it-bpm-oauth2".equalsIgnoreCase(provider.getName());
    }

    private static final GenericTypeMapper<PaymentType, Pair<Type, Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(PaymentType.SEPA, new Pair<>(Type.IBAN, Type.IBAN))
                            .build();
}
