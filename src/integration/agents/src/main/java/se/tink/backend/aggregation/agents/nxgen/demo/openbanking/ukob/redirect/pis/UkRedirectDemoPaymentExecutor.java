package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.ukob.redirect.pis;

import java.util.ArrayList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.ukob.redirect.UkRedirectAuthenticationDemoAgentConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

public class UkRedirectDemoPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;
    private final OAuth2AuthenticationController controller;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final StrongAuthenticationState strongAuthenticationState;
    private PaymentResponse paymentResponse;

    public UkRedirectDemoPaymentExecutor(
            Credentials credentials,
            SupplementalRequester supplementalRequester,
            OAuth2AuthenticationController controller,
            SupplementalInformationHelper supplementalInformationHelper,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            StrongAuthenticationState strongAuthenticationState) {
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
        this.controller = controller;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        this.paymentResponse.getPayment().setStatus(PaymentStatus.SIGNED);
        return this.paymentResponse;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return null;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        // Note: This is not entirely the uk-flow since we're using Oauth2 instead of OpenId. It
        // works in a similar way though so it doesn't matter in this demo case.
        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }

        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth
        // token.
        PersistentStorage dummyStorage = new PersistentStorage();

        this.paymentResponse = new PaymentResponse(paymentRequest.getPayment(), dummyStorage);

        return this.paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(paymentMultiStepRequest);

            case UkRedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE:
                return authorized(paymentMultiStepRequest);

            case UkRedirectAuthenticationDemoAgentConstants.Step.SUFFICIENT_FUNDS:
                return sufficientFunds(paymentMultiStepRequest);

            case UkRedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT:
                return executePayment(paymentMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return null;
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return null;
    }

    private PaymentMultiStepResponse init(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        switch (paymentMultiStepRequest.getPayment().getStatus()) {
            case CREATED:
                // Directly executing the payment after authorizing the payment consent
                // successfully. Steps for funds check needs to be done before authorizing the
                // consent.  Step.AUTHORIZE
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        UkRedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT,
                        new ArrayList<>());
            case REJECTED:
                throw new PaymentAuthorizationException(
                        "Payment is rejected", new IllegalStateException("Payment is rejected"));
            case PENDING:
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        UkRedirectAuthenticationDemoAgentConstants.Step.SUFFICIENT_FUNDS,
                        new ArrayList<>());
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                paymentMultiStepRequest.getPayment().getStatus()));
        }
    }

    private PaymentMultiStepResponse authorized(PaymentMultiStepRequest paymentMultiStepRequest) {
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                UkRedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse sufficientFunds(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                UkRedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse executePayment(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        switch (providerName) {
            case UkRedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_SUCCESS_CASE:
                break;
            case UkRedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_FAILURE_CASE:
                throw UkRedirectAuthenticationDemoAgentConstants.FAILED_CASE_EXCEPTION;
            case UkRedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_CANCEL_CASE:
                throw UkRedirectAuthenticationDemoAgentConstants.CANCELLED_CASE_EXCEPTION;
        }

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                AuthenticationStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }
}
