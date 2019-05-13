package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bunq;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter.BunqRequiredHeadersFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter.BunqSignatureHeaderFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bunq.session.BunqBaseSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
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

public abstract class BunqBaseAgent extends NextGenerationAgent {
    protected final BunqBaseApiClient apiClient;

    public BunqBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        String backendHost = Preconditions.checkNotNull(request.getProvider().getPayload());
        BunqBaseConfiguration agentConfiguration = new BunqBaseConfiguration(backendHost);
        this.apiClient = new BunqBaseApiClient(client, agentConfiguration);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BunqRequiredHeadersFilter(sessionStorage));
        client.addFilter(new BunqSignatureHeaderFilter(persistentStorage, client.getUserAgent()));
        client.setDebugOutput(true);
        client.setDebugProxy("http://192.168.238.15:8888");
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() { return Optional.empty();
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
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
    protected SessionHandler constructSessionHandler() {
        return new BunqBaseSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
