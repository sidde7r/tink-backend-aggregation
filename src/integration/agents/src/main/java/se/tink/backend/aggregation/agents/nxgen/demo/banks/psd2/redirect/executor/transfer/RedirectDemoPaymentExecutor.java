package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

public class RedirectDemoPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;
    private final OAuth2AuthenticationController controller;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final StrongAuthenticationState strongAuthenticationState;
    private PaymentResponse paymentResponse;

    public RedirectDemoPaymentExecutor(
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) throws PaymentException {
        PaymentListResponse paymentListResponse = new PaymentListResponse(this.paymentResponse);
        return paymentListResponse;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
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
        return null;
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
}
