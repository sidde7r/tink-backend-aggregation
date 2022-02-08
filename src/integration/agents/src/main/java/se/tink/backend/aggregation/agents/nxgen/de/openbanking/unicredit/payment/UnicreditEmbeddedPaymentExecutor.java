package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.payment;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditApiClientRetryer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class UnicreditEmbeddedPaymentExecutor extends UnicreditPaymentExecutor {

    private final UnicreditAuthenticator authenticator;
    private final SessionStorage sessionStorage;

    public UnicreditEmbeddedPaymentExecutor(
            UnicreditBaseApiClient apiClient,
            UnicreditApiClientRetryer unicreditApiClientRetryer,
            UnicreditAuthenticator authenticator,
            SessionStorage sessionStorage) {
        super(apiClient, unicreditApiClientRetryer);
        this.authenticator = authenticator;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            authorizePayment();
        }
        return super.sign(paymentMultiStepRequest);
    }

    private void authorizePayment() {
        LinksEntity scaLinks =
                sessionStorage
                        .get(StorageValues.SCA_LINKS, LinksEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
        authenticator.authenticatePayment(scaLinks);
    }
}
