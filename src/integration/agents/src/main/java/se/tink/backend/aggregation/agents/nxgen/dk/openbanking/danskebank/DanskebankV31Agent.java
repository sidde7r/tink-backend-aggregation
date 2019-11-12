package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DanskebankV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder()
                        .withApiBaseURL(V31.AIS_BASE)
                        .withWellKnownURL(V31.WELL_KNOWN)
                        .onMarket("dk")
                        .build();
    }

    public DanskebankV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, aisConfig, true);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage);
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        return DanskebankConfiguration.class;
    }
}
