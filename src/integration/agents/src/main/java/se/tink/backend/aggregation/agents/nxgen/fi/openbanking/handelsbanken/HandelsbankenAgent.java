package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.executor.payment.HandelsbankenPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HandelsbankenAgent extends HandelsbankenBaseAgent {
    private final HandelsbankenAccountConverter accountConverter;

    public HandelsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        throw new IllegalStateException("Authenticator missing");
    }

    @Override
    protected Date setMaxPeriodTransactions() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -HandelsbankenFIConstants.MAX_FETCH_PERIOD_MONTHS);
        sessionStorage.put(HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, date);

        return date;
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        HandelsbankenPaymentExecutorSelector paymentExecutorSelector =
                new HandelsbankenPaymentExecutorSelector(apiClient);
        return Optional.of(new PaymentController(paymentExecutorSelector, paymentExecutorSelector));
    }
}
