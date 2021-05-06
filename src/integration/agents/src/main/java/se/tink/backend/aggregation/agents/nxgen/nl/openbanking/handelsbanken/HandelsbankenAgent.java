package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.executor.payment.HandelsbankenPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class HandelsbankenAgent extends HandelsbankenBaseAgent {

    private static final int MAX_FETCH_PERIOD_MONTHS = 13;
    private final HandelsbankenBaseAccountConverter accountConverter;

    @Inject
    public HandelsbankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        throw new IllegalStateException("Authenticator missing");
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return LocalDate.now().minusMonths(MAX_FETCH_PERIOD_MONTHS);
    }

    @Override
    protected String getMarket() {
        return Market.NETHERLANDS;
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
