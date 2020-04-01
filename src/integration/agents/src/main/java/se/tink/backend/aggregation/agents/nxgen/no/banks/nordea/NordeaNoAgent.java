package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser.NordeaNoParser;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser.NordeaNoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.rpc.filter.NordeaNoFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaNoAgent extends NordeaV17Agent {
    private NordeaNoApiClient nordeaClient;

    public NordeaNoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new NordeaNoParser(new NordeaNoTransactionParser()));
        configureHttpClient(client);
    }

    @Override
    protected NordeaV17ApiClient constructNordeaClient() {
        return nordeaClient =
                new NordeaNoApiClient(
                        client, request.getCredentials(), NordeaNoConstants.MARKET_CODE);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new NordeaNoFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaNoAuthenticator authenticator = new NordeaNoAuthenticator(nordeaClient);
        return new TypedAuthenticationController(
                new BankIdAuthenticationControllerNO(supplementalRequester, authenticator),
                new PasswordAuthenticationController(authenticator));
    }
}
