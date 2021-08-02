package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment;

import java.util.stream.Collectors;
import org.apache.commons.lang.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.InitialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.rpc.CrosskeyPaymentDetails;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CrossKeyPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final CrosskeyBaseApiClient apiClient;
    private final ThirdPartyAppAuthenticationController controller;
    private final Credentials credentials;
    private final SessionStorage sessionStorage;

    public CrossKeyPaymentExecutor(
            CrosskeyBaseApiClient apiClient,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.controller = controller;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        InitialTokenResponse tokenResponse = apiClient.getClientCredentialsToken();
        OAuth2Token oAuth2Token =
                OAuth2Token.createBearer(
                        tokenResponse.getAccessToken(), null, tokenResponse.getExpiresIn());
        apiClient.setTokenToSession(oAuth2Token);

        CrosskeyPaymentDetails consentRequest = CrosskeyPaymentDetails.of(paymentRequest);
        CrosskeyPaymentDetails consentDetails = apiClient.createPaymentConsent(consentRequest);

        sessionStorage.put(StorageKeys.CONSENT, consentDetails);
        try {
            controller.authenticate(credentials);
        } catch (AuthenticationException e) {
            throw new PaymentAuthorizationException(
                    ErrorMessages.NOT_AUTHENTICATED,
                    ThirdPartyAppError.AUTHENTICATION_ERROR.exception());
        } catch (AuthorizationException e) {
            throw new PaymentAuthorizationException(
                    ErrorMessages.NOT_AUTHORIZED,
                    ThirdPartyAppError.AUTHENTICATION_ERROR.exception());
        }

        CrosskeyPaymentDetails paymentResponse = apiClient.makePayment(consentDetails);
        sessionStorage.put(
                StorageKeys.INTERNATIONAL_ID,
                paymentResponse.getData().getInternationalPaymentId());
        return consentDetails.toTinkPayment(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {

        CrosskeyPaymentDetails consentDetails = CrosskeyPaymentDetails.of(paymentMultiStepRequest);
        CrosskeyPaymentDetails paymentResponse = apiClient.makePayment(consentDetails);
        return new PaymentMultiStepResponse(
                paymentResponse.toTinkPayment(paymentMultiStepRequest).getPayment(),
                AuthenticationStepConstants.STEP_FINALIZE);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for class:" + getClass().getSimpleName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not implemented for class:" + getClass().getSimpleName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String internationalId = sessionStorage.get(StorageKeys.INTERNATIONAL_ID);
        CrosskeyPaymentDetails crosskeyPaymentDetails = apiClient.fetchPayment(internationalId);
        return crosskeyPaymentDetails.toTinkPayment(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(paymentRequest -> new PaymentResponse(paymentRequest.getPayment()))
                        .collect(Collectors.toList()));
    }
}
