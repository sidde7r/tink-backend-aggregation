package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NordeaBaseAgent extends NextGenerationAgent {
    protected NordeaBaseApiClient apiClient;

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
                                                ErrorMessages.MISSING_CONFIGURATION));
        apiClient.setConfiguration(nordeaConfiguration);
    }

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
