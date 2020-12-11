package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.JwtSignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModulesForProductionMode(
        modules = {UkOpenBankingModule.class, JwtSignerModule.class})
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class BarclaysV31BusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UKOpenBankingAis.builder()
                        .withOrganisationId(BarclaysConstants.ORGANISATION_ID)
                        .withApiBaseURL(BarclaysConstants.AIS_API_URL)
                        .withWellKnownURL(BarclaysConstants.BUSINESS_WELL_KNOWN_URL)
                        .withPartyEndpoints(
                                UkOpenBankingConstants.PartyEndpoint
                                        .IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES)
                        .withAllowedAccountOwnershipType(AccountOwnershipType.BUSINESS)
                        .build();
    }

    @Inject
    public BarclaysV31BusinessAgent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                new UkOpenBankingPisConfiguration(
                        BarclaysConstants.PIS_API_URL, BarclaysConstants.BUSINESS_WELL_KNOWN_URL),
                createPisRequestFilterUsingPs256WithoutBase64Signature(
                        jwtSigner, componentProvider.getRandomValueGenerator()));
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        UkOpenBankingV31Ais ukOpenBankingV31Ais =
                new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
        return new BarclaysV31Ais(ukOpenBankingV31Ais, aisConfig);
    }
}
