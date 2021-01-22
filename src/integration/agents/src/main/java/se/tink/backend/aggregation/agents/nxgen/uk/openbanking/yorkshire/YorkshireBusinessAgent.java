package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.yorkshire;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingQsealModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingQsealModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public class YorkshireBusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(YorkshireConstants.ORGANISATION_ID)
                        .withWellKnownURL(YorkshireConstants.WELL_KNOWN_URL)
                        .withApiBaseURL(YorkshireConstants.AIS_API_URL)
                        .withPartyEndpoints(
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY)
                        .withAllowedAccountOwnershipType(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public YorkshireBusinessAgent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(componentProvider, jwtSigner, aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected void configureTls(
            UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration) {
        client.setEidasProxy(configuration.getEidasProxy());
    }
}
