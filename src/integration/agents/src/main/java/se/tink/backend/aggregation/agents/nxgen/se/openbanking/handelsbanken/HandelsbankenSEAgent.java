package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HandelsbankenSEAgent extends HandelsbankenBaseAgent {

    private final HandelsbankenAccountConverter accountConverter;

    public HandelsbankenSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected Date setMaxPeriodTransactions() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -HandelsbankenSEConstants.MAX_FETCH_PERIOD_MONTHS);
        sessionStorage.put(HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, date);

        return date;
    }

    @Override
    protected String getMarket() {
        return Market.SWEDEN;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        new HandelsbankenBankIdAuthenticator(apiClient, sessionStorage),
                        persistentStorage,
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final HandelsbankenSEPaymentExecutor paymentExecutor =
                new HandelsbankenSEPaymentExecutor(
                        apiClient, credentials, supplementalRequester, persistentStorage);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
