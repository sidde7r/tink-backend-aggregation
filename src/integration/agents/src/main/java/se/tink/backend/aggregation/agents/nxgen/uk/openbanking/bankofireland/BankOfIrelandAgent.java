package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20AisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland.BankOfIrelandConstants.Urls.V20;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland.BankOfIrelandConstants.Urls.V31;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankOfIrelandAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    static {
        aisConfig =
                new UkOpenBankingV20AisConfiguration.Builder()
                        .withApiBaseURL(V20.AIS_API_URL)
                        .withWellKnownURL(V20.WELL_KNOWN_URL)
                        .build();
    }

    public BankOfIrelandAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, aisConfig, true);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV20Ais(aisConfig, persistentStorage);
    }
}
