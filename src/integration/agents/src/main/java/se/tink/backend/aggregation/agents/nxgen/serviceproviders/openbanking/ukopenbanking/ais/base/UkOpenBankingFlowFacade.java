package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class UkOpenBankingFlowFacade {

    private final TlsConfigurationSetter tlsConfigurationSetter;
    private final JwtSigner jwtSigner;
    private final AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            agentConfiguration;
    private final EidasIdentity ukEidasIdentity;

    public UkOpenBankingFlowFacade(
            TlsConfigurationSetter tlsConfigurationSetter,
            JwtSigner jwtSigner,
            AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
                    agentConfiguration,
            EidasIdentity ukEidasIdentity) {
        this.tlsConfigurationSetter = tlsConfigurationSetter;
        this.jwtSigner = jwtSigner;
        this.agentConfiguration = agentConfiguration;
        this.ukEidasIdentity = ukEidasIdentity;
    }

    public TlsConfigurationSetter getTlsConfigurationSetter() {
        return this.tlsConfigurationSetter;
    }

    public JwtSigner getJwtSinger() {
        return this.jwtSigner;
    }

    public AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration() {
        return this.agentConfiguration;
    }

    public EidasIdentity getUkEidasIdentity() {
        return ukEidasIdentity;
    }
}
