package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.markandspencer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.UkOpenBankingV11Ais;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class MarkAndSpencerAgent extends UkOpenBankingBaseAgent {
    public MarkAndSpencerAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV11Ais();
    }

    @Override
    protected Optional<UkOpenBankingPis> makePis() {
        return Optional.empty();
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {

    }

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {

    }
}
