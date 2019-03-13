package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkParser;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.rpc.filter.NordeaDkFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemidPasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaDkAgentV2 extends NordeaV20Agent {
    private NordeaDkApiClient nordeaClient;
    private NordeaDkSessionStorage nordeaSessionStorage;

    public NordeaDkAgentV2(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair,
                new NordeaDkParser(new NordeaDkTransactionParser(), request.getCredentials()));
    }

    @Override
    protected NordeaV20ApiClient constructNordeaClient() {
        nordeaSessionStorage = new NordeaDkSessionStorage(sessionStorage);

        return nordeaClient = new NordeaDkApiClient(nordeaSessionStorage, client, request.getCredentials(), NordeaDkConstants.MARKET_CODE);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new NordeaDkFilter());
        client.setDebugOutput(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaNemIdAuthenticator nemIdAuthenticator = new NordeaNemIdAuthenticator(nordeaClient, nordeaSessionStorage);

        return new TypedAuthenticationController(
                new PasswordAuthenticationController(
                        new NemidPasswordAuthenticationController(nemIdAuthenticator, 2)
                )
        );
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
