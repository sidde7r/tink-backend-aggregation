package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditPaymentController extends PaymentController {

    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final UnicreditStorage unicreditStorage;
    private final StrongAuthenticationState strongAuthenticationState;

    public UnicreditPaymentController(
            UnicreditPaymentExecutor paymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            UnicreditStorage unicreditStorage,
            StrongAuthenticationState strongAuthenticationState) {
        super(paymentExecutor, paymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.unicreditStorage = unicreditStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl);

        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        unicreditStorage.saveAuthenticationState(strongAuthenticationState.getState());
        PaymentResponse paymentResponse = super.create(paymentRequest);

        String paymentId = paymentResponse.getPayment().getUniqueId();
        URL authorizeUrl = getAuthorizeUrlFromStorage(paymentId);
        openThirdPartyApp(authorizeUrl);
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);

        return paymentResponse;
    }

    private URL getAuthorizeUrlFromStorage(String paymentId) {
        return new URL(
                unicreditStorage
                        .getScaRedirectUrlForPayment(paymentId)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL)));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        return super.sign(paymentMultiStepRequest);
    }
}
