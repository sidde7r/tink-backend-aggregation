package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.UkOpenBankingV11PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20AisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.Urls.V11;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.Urls.V20;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.authenticator.BarclaysAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BarclaysAgent extends UkOpenBankingBaseAgent {

    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    public BarclaysAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new URL(V20.WELL_KNOWN_URL));
        aisConfig = new UkOpenBankingV20AisConfiguration(V20.AIS_API_URL, V20.AIS_AUTH_URL);
        pisConfig = new UkOpenBankingV11PisConfiguration(V11.PIS_API_URL, V11.PIS_AUTH_URL);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV20Ais(aisConfig);
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected Authenticator constructAuthenticator() {
        BarclaysAuthenticator authenticator = new BarclaysAuthenticator(apiClient, aisConfig);
        return createOpenIdFlowWithAuthenticator(authenticator, null);
    }
}
