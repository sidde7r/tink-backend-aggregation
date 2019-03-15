package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NordeaBaseAgent extends NextGenerationAgent {

    public NordeaBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final NordeaBaseConfiguration nordeaConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                NordeaBaseConstants.Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                NordeaBaseConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "NordeaBase configuration missing."));

        persistentStorage.put(
                NordeaBaseConstants.StorageKeys.CLIENT_ID, nordeaConfiguration.getClientId());
        persistentStorage.put(
                NordeaBaseConstants.StorageKeys.CLIENT_SECRET,
                nordeaConfiguration.getClientSecret());
        persistentStorage.put(
                NordeaBaseConstants.StorageKeys.REDIRECT_URI, nordeaConfiguration.getRedirectUrl());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected abstract Authenticator constructAuthenticator();

    @Override
    protected abstract Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController();

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
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
    protected abstract SessionHandler constructSessionHandler();

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
