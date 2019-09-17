package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities.PaymentRedirectInfoEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BecPaymentController extends PaymentController {

    private static final long WAIT_FOR_MINUTES = 5L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SessionStorage sessionStorage;

    public BecPaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage) {
        super(paymentExecutor, fetchablePaymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentRedirectInfoEntity paymentRedirectInfoEntity =
                getPaymentRedirectInfoFromSession(
                        paymentMultiStepRequest.getPayment().getUniqueId());

        collect(
                new URL(paymentRedirectInfoEntity.getRedirectUrl()),
                paymentRedirectInfoEntity.getState());

        return super.sign(paymentMultiStepRequest);
    }

    private void collect(URL url, String state) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(url));

        this.supplementalInformationHelper.waitForSupplementalInformation(
                OAuthUtils.formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    private PaymentRedirectInfoEntity getPaymentRedirectInfoFromSession(String id) {
        return sessionStorage
                .get(id, PaymentRedirectInfoEntity.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        BecConstants.ErrorMessages.MISSING_REDIRECT_INFO));
    }
}
