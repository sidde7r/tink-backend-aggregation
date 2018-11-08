package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.authenticator.NordeaFiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.parser.NordeaFiParser;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.parser.NordeaFiTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21.filter.NordeaFiFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class NordeaFiAgent extends NordeaV21Agent {
    private NordeaFiApiClient fiApiClient;

    public NordeaFiAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair,
                new NordeaFiParser(new NordeaFiTransactionParser(), request.getCredentials()));
    }

    @Override
    protected NordeaV21ApiClient constructNordeaClient() {
        return fiApiClient = new NordeaFiApiClient(client, request.getCredentials(), NordeaFiConstants.MARKET_CODE);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new NordeaFiFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaFiAuthenticator authenticator = new NordeaFiAuthenticator(fiApiClient);
        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
