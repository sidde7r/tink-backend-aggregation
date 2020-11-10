package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import com.google.common.base.Strings;
import java.time.Clock;
import java.util.ArrayList;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.Step;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.UkOpenBankingPisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.helper.UkOpenbankingV31PaymentHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
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
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UKOpenbankingV31Executor implements PaymentExecutor, FetchablePaymentExecutor {
    private static Logger log = LoggerFactory.getLogger(UKOpenbankingV31Executor.class);

    private final SoftwareStatementAssertion softwareStatement;
    private final ClientInfo clientInfo;
    private final UkOpenBankingApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Credentials credentials;
    private final URL appToAppRedirectURL;
    private final StrongAuthenticationState strongAuthenticationState;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenbankingV31PaymentHelper ukOpenbankingV31ExecutorHelper;

    public UKOpenbankingV31Executor(
            SoftwareStatementAssertion softwareStatement,
            ClientInfo clientInfo,
            UkOpenBankingApiClient apiClient,
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

    public UKOpenbankingV31Executor(
            SoftwareStatementAssertion softwareStatement,
            ClientInfo clientInfo,
            UkOpenBankingApiClient apiClient,
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
        this.ukOpenbankingV31ExecutorHelper =
                new UkOpenbankingV31PaymentHelper(this.apiClient, Clock.systemUTC());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        UkOpenBankingV31PisUtils.validateRemittanceWithProviderOrThrow(
                credentials.getProviderName(),
                paymentRequest.getPayment().getRemittanceInformation());
        return authenticateAndCreatePisConsent(paymentRequest);
    }

    private PaymentResponse authenticateAndCreatePisConsent(PaymentRequest paymentRequest)
            throws PaymentAuthorizationException {

        UkOpenBankingPisAuthenticator paymentAuthenticator =
                new UkOpenBankingPisAuthenticator(
                        apiClient,
                        ukOpenbankingV31ExecutorHelper,
                        softwareStatement,
                        clientInfo,
                        paymentRequest);

        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth
        // token.
        PersistentStorage dummyStorage = new PersistentStorage();

        UkOpenBankingAuthenticationController openIdAuthenticationController =
                new UkOpenBankingAuthenticationController(
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

            throw UkOpenBankingV31PisUtils.createFailedTransferException();
        }

        return paymentAuthenticator.getPaymentResponse();
    }

    private boolean hasWellKnownOpenIdError(UkOpenBankingApiClient apiClient) {
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
        return UkOpenBankingV31Constants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)
                || UkOpenBankingV31Constants.Errors.LOGIN_REQUIRED.equalsIgnoreCase(errorType);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return ukOpenbankingV31ExecutorHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(
                paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(paymentMultiStepRequest);

            case UkOpenBankingV31Constants.Step.AUTHORIZE:
                return authorized(paymentMultiStepRequest);

            case UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS:
                return sufficientFunds(paymentMultiStepRequest);

            case UkOpenBankingV31Constants.Step.EXECUTE_PAYMENT:
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
                        UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS,
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
                                ukOpenbankingV31ExecutorHelper
                                        .fetchPaymentIfAlreadyExecutedOrGetConsent(
                                                paymentMultiStepRequest))
                        .map(p -> p.getPayment().getStatus())
                        .filter(s -> s == PaymentStatus.PENDING)
                        .map(s -> UkOpenBankingV31Constants.Step.SUFFICIENT_FUNDS)
                        .orElse(UkOpenBankingV31Constants.Step.AUTHORIZE);

        return new PaymentMultiStepResponse(paymentMultiStepRequest, step, new ArrayList<>());
    }

    private PaymentMultiStepResponse sufficientFunds(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException {

        final Optional<FundsConfirmationResponse> maybeFundsConfirmation =
                ukOpenbankingV31ExecutorHelper.fetchFundsConfirmation(paymentMultiStepRequest);

        if (maybeFundsConfirmation.isPresent()
                && !maybeFundsConfirmation.get().isFundsAvailable()) {
            throw new InsufficientFundsException(
                    "Insufficient funds", "", new IllegalStateException("Insufficient funds"));
        }

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                UkOpenBankingV31Constants.Step.EXECUTE_PAYMENT,
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
                ukOpenbankingV31ExecutorHelper.executePayment(
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
}
