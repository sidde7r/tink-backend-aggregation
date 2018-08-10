package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankAbstractAgent;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class SwedbankSEAgent extends SwedbankAbstractAgent {

    public SwedbankSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SwedbankSEConfiguration(request.getProvider().getPayload()));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setProxy("http://127.0.0.1:8888");
        super.configureHttpClient(client);
    }
}
