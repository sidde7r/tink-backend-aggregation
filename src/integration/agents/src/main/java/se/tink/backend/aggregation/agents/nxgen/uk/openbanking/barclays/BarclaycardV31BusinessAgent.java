package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.Urls.V31;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingLocalKeySignerModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities(CREDIT_CARDS)
public final class BarclaycardV31BusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UKOpenBankingAis.builder()
                        .withOrganisationId(BarclaysConstants.ORGANISATION_ID)
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.BARCLAYCARD_BUSINESS_WELL_KNOWN_URL)
                        .withAllowedAccountOwnershipType(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public BarclaycardV31BusinessAgent(
            AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(componentProvider, jwtSigner, aisConfig, true);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        UkOpenBankingV31Ais ukOpenBankingV31Ais =
                new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
        return new BarclaysV31Ais(ukOpenBankingV31Ais, aisConfig);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }
}
