package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.JwksClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.JwksKeySigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public final class JwksKeySignerProvider implements Provider<JwtSigner> {

    private final UkOpenBankingConfiguration configuration;
    private final TinkHttpClientProvider httpClientProvider;

    @Inject
    private JwksKeySignerProvider(
            UkOpenBankingConfiguration configuration, AgentComponentProvider httpClientProvider) {
        this.configuration = configuration;
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public JwtSigner get() {
        return new JwksKeySigner(
                configuration.getSigningKey(),
                configuration.getSoftwareStatementAssertions().getJwksEndpoint(),
                new JwksClient(httpClientProvider.getTinkHttpClient()));
    }
}
