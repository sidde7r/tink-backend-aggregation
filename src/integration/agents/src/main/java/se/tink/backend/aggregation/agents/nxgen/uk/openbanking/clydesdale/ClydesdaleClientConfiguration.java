package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.clydesdale;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClydesdaleClientConfiguration extends UkOpenBankingConfiguration {

    @Override
    public Optional<TlsConfigurationSetter> getTlsConfigurationOverride() {
        return Optional.empty();
    }
}
