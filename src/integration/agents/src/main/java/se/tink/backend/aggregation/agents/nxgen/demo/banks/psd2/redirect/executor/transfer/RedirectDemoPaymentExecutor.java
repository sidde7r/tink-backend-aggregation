package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectDemoAgentUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

public class RedirectDemoPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final Credentials credentials;
    private final OAuth2AuthenticationController controller;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final StrongAuthenticationState strongAuthenticationState;
    private PaymentResponse paymentResponse;
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RedirectDemoPaymentExecutor(
            Credentials credentials,
            OAuth2AuthenticationController controller,
            SupplementalInformationHelper supplementalInformationHelper,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            StrongAuthenticationState strongAuthenticationState) {
        this.credentials = credentials;
        this.controller = controller;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        this.paymentResponse.getPayment().setStatus(PaymentStatus.SIGNED);
        return this.paymentResponse;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        this.paymentResponse.getPayment().setStatus(PaymentStatus.CREATED);
        return new PaymentListResponse(this.paymentResponse);

        // todo: fix or remove this!
        // this.paymentResponse.getPayment().setStatus(PaymentStatus.CREATED);
        //        return paymentListRequest.getPaymentRequestList().stream()
        //                .map(this::fetch)
        //                .collect(
        //                        Collectors.collectingAndThen(
        //                                Collectors.toList(), PaymentListResponse::new));
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        logger.info("Demo Provider: creating payment via Payment Executor");
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
        logger.info("Demo Provider: signing payment via Payment Executor");
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(paymentMultiStepRequest);

            case RedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE:
                return authorized(paymentMultiStepRequest);

            case RedirectAuthenticationDemoAgentConstants.Step.SUFFICIENT_FUNDS:
                return sufficientFunds(paymentMultiStepRequest);

            case RedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT:
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
                        RedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT,
                        new ArrayList<>());
            case REJECTED:
                throw new PaymentAuthorizationException(
                        "Payment is rejected", new IllegalStateException("Payment is rejected"));
            case PENDING:
                return new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        RedirectAuthenticationDemoAgentConstants.Step.SUFFICIENT_FUNDS,
                        new ArrayList<>());
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                paymentMultiStepRequest.getPayment().getStatus()));
        }
    }

    private PaymentMultiStepResponse authorized(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String step =
                Optional.of(fetch(paymentMultiStepRequest))
                        .map(p -> p.getPayment().getStatus())
                        .filter(s -> s == PaymentStatus.PENDING)
                        .map(s -> RedirectAuthenticationDemoAgentConstants.Step.SUFFICIENT_FUNDS)
                        .orElse(RedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE);

        return new PaymentMultiStepResponse(paymentMultiStepRequest, step, new ArrayList<>());
    }

    private PaymentMultiStepResponse sufficientFunds(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                RedirectAuthenticationDemoAgentConstants.Step.EXECUTE_PAYMENT,
                new ArrayList<>());
    }

    private PaymentMultiStepResponse executePayment(
            PaymentMultiStepRequest paymentMultiStepRequest) {

        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        RedirectDemoAgentUtils.failPaymentIfFailStateProvider(providerName);

        PaymentMultiStepResponse pmr =
                new PaymentMultiStepResponse(
                        paymentMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
        pmr.getPayment().setStatus(PaymentStatus.PAID);

        return pmr;
    }
}
