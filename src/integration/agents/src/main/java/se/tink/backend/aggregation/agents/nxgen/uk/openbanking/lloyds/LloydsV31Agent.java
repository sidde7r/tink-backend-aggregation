package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Agent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank.authenticator.DanskeBankAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LloydsV31Agent extends UkOpenBankingV31Agent {

    public LloydsV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new LloydsV31Configuration(),
                new LloydsV31Configuration());
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais();
    }

    @Override
    protected Optional<UkOpenBankingPis> makePis() {
        return Optional.empty();
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankAuthenticator authenticator = new DanskeBankAuthenticator(apiClient);
        return createOpenIdFlowWithAuthenticator(authenticator, false);
    }
}
