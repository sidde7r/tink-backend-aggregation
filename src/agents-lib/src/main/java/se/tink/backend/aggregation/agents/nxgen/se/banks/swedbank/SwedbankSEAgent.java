package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankAbstractAgent;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class SwedbankSEAgent extends SwedbankAbstractAgent {

    public SwedbankSEAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath, new SwedbankSEConfiguration(request.getProvider().getPayload()));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        super.configureHttpClient(client);
    }
}
