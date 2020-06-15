package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.payment.HandelsbankenPaymentExecutorSelector;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HandelsbankenAgent extends HandelsbankenBaseAgent {
    private final HandelsbankenAccountConverter accountConverter;
    private static final int MAX_FETCH_PERIOD_MONTHS = 13;

    public HandelsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        throw new IllegalStateException("Authenticator missing");
    }

    @Override
    protected String getMarket() {
        return Market.GREAT_BRITAIN;
    }

    @Override
    protected Date setMaxPeriodTransactions() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -MAX_FETCH_PERIOD_MONTHS);
        persistentStorage.put(
                HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, calendar.getTime());

        return calendar.getTime();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new PaymentController(new HandelsbankenPaymentExecutorSelector(apiClient)));
    }
}
