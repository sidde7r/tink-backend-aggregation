package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

public interface UkOpenBankingClientConfigurationAdapter extends ClientConfiguration {

    ProviderConfiguration getProviderConfiguration();

    SoftwareStatementAssertion getSoftwareStatementAssertions();

    Optional<TlsConfigurationOverride> getTlsConfigurationOverride();

    Optional<JwtSigner> getSignerOverride();
}
