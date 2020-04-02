package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UnicreditPaymentController extends PaymentController {

    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;

    public UnicreditPaymentController(
            UnicreditPaymentExecutor paymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage) {
        super(paymentExecutor, paymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl);

        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse paymentResponse = super.create(paymentRequest);

        String id = paymentResponse.getPayment().getUniqueId();
        URL authorizeUrl = getAuthorizeUrlFromStorage(id);
        openThirdPartyApp(authorizeUrl);

        this.supplementalInformationHelper.waitForSupplementalInformation(
                this.formatSupplementalKey(
                        persistentStorage.get(UnicreditConstants.StorageKeys.STATE)),
                WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        return paymentResponse;
    }

    private URL getAuthorizeUrlFromStorage(String id) {
        return new URL(
                persistentStorage
                        .get(id, String.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL)));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        return super.sign(paymentMultiStepRequest);
    }

    private String formatSupplementalKey(String key) {
        return String.format("tpcb_%s", key);
    }
}
