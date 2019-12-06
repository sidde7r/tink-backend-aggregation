package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Xs2aDevelopersPaymentController extends PaymentController {
    private final ThirdPartyAppAuthenticationController controller;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    public Xs2aDevelopersPaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        super(paymentExecutor, fetchablePaymentExecutor);
        this.controller = controller;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        try {
            persistentStorage.put(
                    StorageKeys.PAYMENT_ID, paymentRequest.getPayment().getUniqueId());
            controller.authenticate(credentials);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
        return super.fetch(paymentRequest);
    }
}
