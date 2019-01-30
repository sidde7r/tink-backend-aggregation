package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser.NordeaNoParser;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser.NordeaNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.rpc.filter.NordeaNoFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class NordeaNoAgent extends NordeaV17Agent {
    private NordeaNoApiClient nordeaClient;

    public NordeaNoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair,
                new NordeaNoParser(new NordeaNoTransactionParser()));
    }

    @Override
    protected NordeaV17ApiClient constructNordeaClient() {
        return nordeaClient = new NordeaNoApiClient(client, request.getCredentials(), NordeaNoConstants.MARKET_CODE);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new NordeaNoFilter());
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaNoAuthenticator authenticator = new NordeaNoAuthenticator(nordeaClient);
        return new TypedAuthenticationController(
                new BankIdAuthenticationControllerNO(context, authenticator),
                new PasswordAuthenticationController(authenticator));
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
