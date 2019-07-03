package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31AisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds.LloydsConstants.Urls.V31;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds.authenticator.LloydsAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LloydsV31Agent extends UkOpenBankingBaseAgent {

    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    public LloydsV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new URL(V31.WELL_KNOWN_URL));
        aisConfig = new UkOpenBankingV31AisConfiguration(V31.AIS_API_URL, V31.AIS_AUTH_URL);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL, V31.AIS_AUTH_URL);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig);
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected Authenticator constructAuthenticator() {
        LloydsAuthenticator authenticator = new LloydsAuthenticator(apiClient, aisConfig);
        return createOpenIdFlowWithAuthenticator(authenticator, false);
    }
}
