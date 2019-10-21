package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.executor.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditUserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UnicreditPaymentController extends PaymentController {

    private final PersistentStorage persistentStorage;
    private final UnicreditApiClient apiClient;
    private final Credentials credentials;

    public UnicreditPaymentController(
            UnicreditPaymentExecutor paymentExecutor,
            UnicreditApiClient apiClient,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        super(paymentExecutor, paymentExecutor);

        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse paymentResponse = super.create(paymentRequest);

        String id = paymentResponse.getPayment().getUniqueId();
        URL authorizeUrl = getAuthorizeUrlFromStorage(id);
        String password = credentials.getField(Key.PASSWORD);

        UnicreditUserDataResponse unicreditUserDataResponse =
                apiClient.getUnicreditUserDataResponse(password, authorizeUrl);

        apiClient.updateConsentWithOtp(unicreditUserDataResponse.getScaRedirect());

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
}
