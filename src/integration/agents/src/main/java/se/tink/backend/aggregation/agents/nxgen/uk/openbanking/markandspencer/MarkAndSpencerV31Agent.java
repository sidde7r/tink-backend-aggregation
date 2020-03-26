package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.markandspencer;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.markandspencer.MarksAndSpencerConstants.Urls.V31;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class MarkAndSpencerV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final LocalDateTimeSource localDateTimeSource;

    static {
        aisConfig =
                new UKOpenBankingAis.Builder()
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.WELL_KNOWN_URL)
                        .withIdentityDataURL(PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .withAdditionalPermission(
                                PartyEndpoints.partyEndpointsPermissionMap.get(
                                        PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY))
                        .build();
    }

    @Inject
    public MarkAndSpencerV31Agent(AgentComponentProvider componentProvider) {
        super(componentProvider, aisConfig, false);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }
}
