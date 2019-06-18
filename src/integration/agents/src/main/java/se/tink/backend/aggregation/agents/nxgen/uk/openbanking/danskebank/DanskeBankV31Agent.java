package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

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

public class DanskeBankV31Agent extends UkOpenBankingV31Agent {

    public DanskeBankV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new DanskeBankV31Configuration(),
                new DanskeBankV31Configuration());
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais();
    }

    @Override
    protected Optional<UkOpenBankingPis> makePis() {
        // We will add PIS Controller for v3.1 after we validate it
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
