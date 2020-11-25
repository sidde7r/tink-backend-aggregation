package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants.Step;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.OpenIdPisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domesticscheduled.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.ApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.DomesticPaymentApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.DomesticScheduledPaymentApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.UkOpenBankingPaymentHelper;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UkOpenBankingExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private static final Logger log = LoggerFactory.getLogger(UkOpenBankingExecutor.class);

    private final SoftwareStatementAssertion softwareStatement;
    private final ClientInfo clientInfo;
    private final UkOpenBankingPaymentApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Credentials credentials;
    private final URL appToAppRedirectURL;
    private final StrongAuthenticationState strongAuthenticationState;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenBankingPaymentHelper ukOpenBankingPaymentHelper;

    public UkOpenBankingExecutor(
            SoftwareStatementAssertion softwareStatement,
            ClientInfo clientInfo,
            UkOpenBankingPaymentApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            RandomValueGenerator randomValueGenerator) {

        this(
                softwareStatement,
                clientInfo,
                apiClient,
                supplementalInformationHelper,
                credentials,
                strongAuthenticationState,
                randomValueGenerator,
                null);
    }

    private UkOpenBankingExecutor(
            SoftwareStatementAssertion softwareStatement,
            ClientInfo clientInfo,
            UkOpenBankingPaymentApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            RandomValueGenerator randomValueGenerator,
            URL appToAppRedirectURL) {
        this.softwareStatement = softwareStatement;
        this.clientInfo = clientInfo;
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.credentials = credentials;
        this.appToAppRedirectURL = appToAppRedirectURL;
        this.strongAuthenticationState = strongAuthenticationState;
        this.randomValueGenerator = randomValueGenerator;
        this.ukOpenBankingPaymentHelper =
                new UkOpenBankingPaymentHelper(this.createApiClientWrapperMap(), Clock.systemUTC());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                credentials.getProviderName(),
                paymentRequest.getPayment().getRemittanceInformation());
        return authenticateAndCreatePisConsent(paymentRequest);
    }

    private PaymentResponse authenticateAndCreatePisConsent(PaymentRequest paymentRequest)
            throws PaymentAuthorizationException {

        OpenIdPisAuthenticator paymentAuthenticator =
                new OpenIdPisAuthenticator(
                        apiClient,
                        ukOpenBankingPaymentHelper,
                        softwareStatement,
                        clientInfo,
                        paymentRequest);

        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth
        // token.
        PersistentStorage dummyStorage = new PersistentStorage();

        OpenIdAuthenticationController openIdAuthenticationController =
                new OpenIdAuthenticationController(
                        dummyStorage,
                        supplementalInformationHelper,
                        apiClient,
                        paymentAuthenticator,
                        credentials,
                        strongAuthenticationState,
                        null,
                        appToAppRedirectURL,
                        randomValueGenerator);

        ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController, supplementalInformationHelper);

        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);

        } catch (AuthenticationException | AuthorizationException e) {

            if (e.getError() instanceof ThirdPartyAppError
                    && ThirdPartyAppError.TIMED_OUT.equals(e.getError())) {
                throw new PaymentAuthorizationTimeOutException();
            }

            if (hasWellKnownOpenIdError(apiClient)) {
                ErrorEntity errorEntity = apiClient.getErrorEntity().get();

                String errorMessage = errorEntity.getErrorMessage();
                ExceptionFuzzyMatcher exceptionMatcher = new ExceptionFuzzyMatcher();
                if (Strings.isNullOrEmpty(errorMessage)) {
                    throw new PaymentAuthorizationException();
                } else if (exceptionMatcher.isAuthorizationCancelledByUser(errorEntity)) {
                    throw new PaymentAuthorizationCancelledByUserException(errorEntity);
                } else if (exceptionMatcher.isAuthorizationTimeOut(errorEntity)) {
                    throw new PaymentAuthorizationTimeOutException(errorEntity);
                } else if (exceptionMatcher.isAuthorizationFailedByUser(errorEntity)) {
                    throw new PaymentAuthorizationFailedByUserException(errorEntity);
                } else {
                    // Log unknown error message and return the generic end user message for when
                    // payment wasn't authorised.
                    log.warn(
                            "Unknown error message from bank during payment authorisation: {}",
                            errorMessage);
                    throw new PaymentAuthorizationException();
                }
            }

            throw UkOpenBankingPisUtils.createFailedTransferException();
        }

        return paymentAuthenticator.getPaymentResponse();
    }

    private boolean hasWellKnownOpenIdError(UkOpenBankingPaymentApiClient apiClient) {
        if (!apiClient.getErrorEntity().isPresent()) {
            return false;
        }

        ErrorEntity errorEntity = apiClient.getErrorEntity().get();
        return isKnownOpenIdError(errorEntity.getErrorType());
    }

    /**
     * Currently known errors are access_denied and login_required. According to openId
     * documentation access_denied means that resource owner (end user) did not provide consent.
     * login_required means that user didn't authenticate at all.
     */
    private boolean isKnownOpenIdError(String errorType) {
        return UkOpenBankingV31PaymentConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)
                || UkOpenBankingV31PaymentConstants.Errors.LOGIN_REQUIRED.equalsIgnoreCase(
                        errorType);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(paymentMultiStepRequest);

            case UkOpenBankingV31PaymentConstants.Step.AUTHORIZE:
                return authorized(paymentMultiStepRequest);

            case UkOpenBankingV31PaymentConstants.Step.SUFFICIENT_FUNDS:
                return sufficientFunds(paymentMultiStepRequest);

            case UkOpenBankingV31PaymentConstants.Step.EXECUTE_PAYMENT:
                return executePayment(paymentMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
    }

    private PaymentMultiStepResponse init(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        switch (paymentMultiStepRequest.getPayment().getStatus()) {
            case CREATED:
                // Directly executing the payment after authorizing the payment consent
                // successfully. Steps for funds check needs to be done before authorizing the
                // consent.  Step.AUTHORIZE
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest, Step.EXECUTE_PAYMENT, new ArrayList<>());
            case REJECTED:
                throw new PaymentAuthorizationException(
                        "Payment is rejected", new IllegalStateException("Payment is rejected"));
            case PENDING:
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        UkOpenBankingV31PaymentConstants.Step.SUFFICIENT_FUNDS,
                        new ArrayList<>());
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                paymentMultiStepRequest.getPayment().getStatus()));
        }
    }

    private PaymentMultiStepResponse authorized(PaymentMultiStepRequest paymentMultiStepRequest) {
        String step =
                Optional.of(
                                ukOpenBankingPaymentHelper
                                        .fetchPaymentIfAlreadyExecutedOrGetConsent(
                                                paymentMultiStepRequest))
                        .map(p -> p.getPayment().getStatus())
                        .filter(s -> s == PaymentStatus.PENDING)
                        .map(s -> UkOpenBankingV31PaymentConstants.Step.SUFFICIENT_FUNDS)
                        .orElse(UkOpenBankingV31PaymentConstants.Step.AUTHORIZE);

        return new PaymentMultiStepResponse(paymentMultiStepRequest, step, new ArrayList<>());
    }

    private PaymentMultiStepResponse sufficientFunds(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException {

        final Optional<FundsConfirmationResponse> maybeFundsConfirmation =
                ukOpenBankingPaymentHelper.fetchFundsConfirmation(paymentMultiStepRequest);

        if (maybeFundsConfirmation.isPresent()
                && !maybeFundsConfirmation.get().isFundsAvailable()) {
            throw new InsufficientFundsException(
                    "Insufficient funds", "", new IllegalStateException("Insufficient funds"));
        }

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                UkOpenBankingV31PaymentConstants.Step.EXECUTE_PAYMENT,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse executePayment(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        RemittanceInformation remittanceInformation =
                paymentMultiStepRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        String endToEndIdentification =
                paymentMultiStepRequest.getPayment().getUniqueIdForUKOPenBanking();
        String instructionIdentification = paymentMultiStepRequest.getPayment().getUniqueId();

        PaymentResponse paymentResponse =
                ukOpenBankingPaymentHelper.executePayment(
                        paymentMultiStepRequest, endToEndIdentification, instructionIdentification);

        // Should be handled on a higher level than here, but don't want to pollute the
        // payment controller with TransferExecutionException usage. Ticket PAY2-188 will
        // address handling the REJECTED status, then we can remove the logic from here.
        if (PaymentStatus.REJECTED.equals(paymentResponse.getPayment().getStatus())) {
            throw new PaymentRejectedException();
        }

        return new PaymentMultiStepResponse(
                paymentResponse, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private Map<PaymentType, ApiClientWrapper> createApiClientWrapperMap() {
        final DomesticPaymentApiClientWrapper domesticPaymentApiClientWrapper =
                new DomesticPaymentApiClientWrapper(this.apiClient, new DomesticPaymentConverter());

        return ImmutableMap.of(
                PaymentType.DOMESTIC,
                domesticPaymentApiClientWrapper,
                PaymentType.DOMESTIC_FUTURE,
                new DomesticScheduledPaymentApiClientWrapper(
                        this.apiClient, new DomesticScheduledPaymentConverter()),
                PaymentType.SEPA,
                domesticPaymentApiClientWrapper);
    }
}
