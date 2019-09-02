package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.PaymentRedirectInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.SignOptionsResponse;
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

public class SbabPaymentController extends PaymentController {

    private static final long WAIT_FOR_MINUTES = 5L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SessionStorage sessionStorage;

    public SbabPaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage) {
        super(paymentExecutor, fetchablePaymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.sessionStorage = sessionStorage;
    }

    private void collect(URL url, String state) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(url));

        this.supplementalInformationHelper.waitForSupplementalInformation(
                OAuthUtils.formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentRedirectInfoEntity paymentRedirectInfoEntity =
                getPaymentRedirectInfoFromSession(
                        paymentMultiStepRequest.getPayment().getUniqueId());

        SignOptionsResponse signOptionsResponse =
                paymentRedirectInfoEntity.getSignOptionsResponse();

        // No signing is necessary if it's a transfer between accounts of one person
        // so in that case no sign options are returned
        if (signOptionsResponse != null) {
            collect(
                    new URL(signOptionsResponse.getBankIdSignRedirectUrl()),
                    paymentRedirectInfoEntity.getState());
        }

        return super.sign(paymentMultiStepRequest);
    }

    private PaymentRedirectInfoEntity getPaymentRedirectInfoFromSession(String id) {
        return sessionStorage
                .get(id, PaymentRedirectInfoEntity.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        ErrorMessages.MISSING_PAYMENT_REDIRECT_INFO));
    }
}
