package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface UkOpenBankingClientConfigurationAdapter extends ClientConfiguration {

    ClientInfo getProviderConfiguration();

    SoftwareStatementAssertion getSoftwareStatementAssertions();

    Optional<TlsConfigurationSetter> getTlsConfigurationOverride();
}
