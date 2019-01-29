package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.AmericanExpressPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.AmericanExpressTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.AmericanExpressSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressAgent extends NextGenerationAgent {

    private final AmericanExpressApiClient apiClient;
    private final AmericanExpressConfiguration config;

    protected AmericanExpressAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            AmericanExpressConfiguration config) {
        super(request, context, signatureKeyPair);
        generateDeviceId();
        this.apiClient = new AmericanExpressApiClient(client, sessionStorage, config);
        this.config = config;
    }

    private void generateDeviceId() {
        String uid = credentials.getField(Field.Key.USERNAME);
        String deviceId = StringUtils.hashAsUUID(uid);
        sessionStorage.put(AmericanExpressConstants.Tags.HARDWARE_ID, deviceId);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        // Amex is throttling how many requests we can send per IP address.
        // Use this multiIp gateway to originate from different IP addresses.
        setMultiIpGateway(configuration.getIntegrations());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) { }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new AmericanExpressPasswordAuthenticator(apiClient, config, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
    constructTransactionalAccountRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new AmericanExpressCreditCardAccountFetcher(sessionStorage, config),
                        new AmericanExpressTransactionFetcher(apiClient, config)));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
    constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AmericanExpressSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
