package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FinecoBankPaymentController extends PaymentController {
    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;

    public FinecoBankPaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState) {
        super(paymentExecutor, fetchablePaymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    private ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    private ThirdPartyAppResponse<String> collect() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        sessionStorage.put(
                FinecoBankConstants.StorageKeys.STATE, strongAuthenticationState.getState());
        return super.create(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        if (paymentMultiStepRequest
                .getStep()
                .equalsIgnoreCase(AuthenticationStepConstants.STEP_INIT)) {
            init();
            URL authorizeUrl =
                    new URL(sessionStorage.get(paymentMultiStepRequest.getPayment().getUniqueId()));
            ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
            collect();
            return new PaymentMultiStepResponse(
                    paymentMultiStepRequest.getPayment(),
                    FinecoBankConstants.FinecoBankSignSteps.SAMPLE_STEP,
                    new ArrayList<>());
        } else {
            return super.sign(paymentMultiStepRequest);
        }
    }
}
