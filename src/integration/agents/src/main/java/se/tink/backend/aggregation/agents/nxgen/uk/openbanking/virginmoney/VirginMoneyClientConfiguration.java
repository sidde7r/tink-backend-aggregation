package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.virginmoney;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;

public class VirginMoneyClientConfiguration extends UkOpenBankingConfiguration {

    @Override
    public Optional<TlsConfigurationSetter> getTlsConfigurationOverride() {
        return Optional.empty();
    }
}
