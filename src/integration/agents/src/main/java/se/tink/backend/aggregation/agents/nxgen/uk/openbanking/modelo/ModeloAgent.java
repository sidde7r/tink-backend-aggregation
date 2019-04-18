package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.modelo;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.UkOpenBankingV11Pis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Configuration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ModeloAgent extends UkOpenBankingV30Agent {
    public ModeloAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV30Ais();
    }

    @Override
    protected Optional<UkOpenBankingPis> makePis() {
        return Optional.of(new UkOpenBankingV11Pis());
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {
        httpClient.setDebugOutput(true);
    }

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}
}
