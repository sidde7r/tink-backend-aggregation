package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

public interface UkOpenBankingClientConfigurationAdapter extends ClientConfiguration {

    byte[] getRootCAData();

    String getRootCAPassword();

    ProviderConfiguration getProviderConfiguration();

    SoftwareStatementAssertion getSoftwareStatementAssertion();

    Optional<TlsConfigurationOverride> getTlsConfigurationOverride();

    JwtSigner getSigner();
}
