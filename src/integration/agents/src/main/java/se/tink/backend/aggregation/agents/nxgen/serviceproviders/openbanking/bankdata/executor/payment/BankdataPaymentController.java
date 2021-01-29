package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
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

public class BankdataPaymentController extends PaymentController {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SessionStorage sessionStorage;

    public BankdataPaymentController(
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

    public ThirdPartyAppResponse<String> collect() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        sessionStorage.put(
                BankdataConstants.StorageKeys.STATE, strongAuthenticationState.getState());
        return super.create(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        if (paymentMultiStepRequest.getStep() == AuthenticationStepConstants.STEP_INIT) {
            PaymentMultiStepResponse multiStepResponse = super.sign(paymentMultiStepRequest);
            init();
            openThirdPartyApp(
                    new URL(
                            getSigningUrlFromSession(
                                    paymentMultiStepRequest.getPayment().getUniqueId())));
            collect();
            return multiStepResponse;
        }

        return super.sign(paymentMultiStepRequest);
    }

    private String getSigningUrlFromSession(String paymentId) {
        return sessionStorage
                .get(paymentId, String.class)
                .orElseThrow(() -> new IllegalStateException("Signing url could not be retrived"));
    }
}
