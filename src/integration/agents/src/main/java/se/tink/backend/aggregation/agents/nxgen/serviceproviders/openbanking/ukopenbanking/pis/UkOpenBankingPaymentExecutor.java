package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.OpenIdPisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.OpenIdPisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domesticscheduled.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class UkOpenBankingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    @VisibleForTesting static final String CLIENT_TOKEN = "CLIENT_OAUTH2_TOKEN";

    private final SoftwareStatementAssertion softwareStatement;
    private final ClientInfo clientInfo;
    private final UkOpenBankingPaymentApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Credentials credentials;
    private final StrongAuthenticationState strongAuthenticationState;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenBankingPaymentHelper ukOpenBankingPaymentHelper;
    private final PersistentStorage persistentStorage;

    public UkOpenBankingPaymentExecutor(
            SoftwareStatementAssertion softwareStatement,
            ClientInfo clientInfo,
            UkOpenBankingPaymentApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage) {

        this.softwareStatement = softwareStatement;
        this.clientInfo = clientInfo;
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.credentials = credentials;
        this.strongAuthenticationState = strongAuthenticationState;
        this.randomValueGenerator = randomValueGenerator;
        this.ukOpenBankingPaymentHelper =
                new UkOpenBankingPaymentHelper(this.createApiClientWrapperMap(), Clock.systemUTC());
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                credentials.getProviderName(),
                paymentRequest.getPayment().getRemittanceInformation());

        fetchAndStoreClientToken();

        return createConsentWithRetry(paymentRequest);
    }

    /**
     * For fixing the Barclays unstable issue; No-sleep retry had been tested but working not well;
     * No-sleep retry will get continuous rejection; Jira had been raised on UKOB directory by other
     * TPPs
     */
    private PaymentResponse createConsentWithRetry(PaymentRequest paymentRequest) {
        for (int i = 0; i < 3; i++) {
            try {
                return ukOpenBankingPaymentHelper.createConsent(paymentRequest);
            } catch (HttpResponseException e) {
                Uninterruptibles.sleepUninterruptibly(2000 * i, TimeUnit.MILLISECONDS);
            }
        }

        return ukOpenBankingPaymentHelper.createConsent(paymentRequest);
    }

    private void fetchAndStoreClientToken() {
        final OAuth2Token clientOAuth2Token = retrieveClientToken();

        persistentStorage.put(CLIENT_TOKEN, clientOAuth2Token);

        apiClient.instantiatePisAuthFilter(clientOAuth2Token);
    }

    private PaymentMultiStepResponse authenticate(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        final String intentId = paymentMultiStepRequest.getStorage().get("consentId");

        OpenIdPisAuthenticator paymentAuthenticator =
                new OpenIdPisAuthenticator(apiClient, softwareStatement, clientInfo, intentId);

        OpenIdAuthenticationController openIdAuthenticationController =
                new OpenIdPisAuthenticationController(
                        supplementalInformationHelper,
                        apiClient,
                        paymentAuthenticator,
                        credentials,
                        strongAuthenticationState,
                        null,
                        randomValueGenerator,
                        getClientTokenFromStorage());

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

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                ExecutorSignStep.EXECUTE_PAYMENT.name(),
                new ArrayList<>());
    }

    private boolean hasWellKnownOpenIdError(UkOpenBankingPaymentApiClient apiClient) {
        return apiClient
                .getErrorEntity()
                .map(ErrorEntity::getErrorType)
                .map(UkOpenBankingPaymentExecutor::isKnownOpenIdError)
                .orElse(Boolean.FALSE);
    }

    /**
     * Currently known errors are access_denied and login_required. According to openId
     * documentation access_denied means that resource owner (end user) did not provide consent.
     * login_required means that user didn't authenticate at all.
     */
    private static boolean isKnownOpenIdError(String errorType) {
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

        final ExecutorSignStep step = ExecutorSignStep.of(paymentMultiStepRequest.getStep());
        switch (step) {
            case AUTHENTICATE:
                return authenticate(paymentMultiStepRequest);

            case EXECUTE_PAYMENT:
                return executePayment(paymentMultiStepRequest);
            default:
                throw new IllegalArgumentException(
                        "Unknown step: " + paymentMultiStepRequest.getStep());
        }
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

    private OAuth2Token retrieveClientToken() {
        final OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials();
        if (!clientOAuth2Token.isValid()) {
            throw new IllegalArgumentException("Client access token is not valid.");
        }

        return clientOAuth2Token;
    }

    private OAuth2Token getClientTokenFromStorage() {
        return persistentStorage
                .get(CLIENT_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Client token not found in the storage"));
    }
}
