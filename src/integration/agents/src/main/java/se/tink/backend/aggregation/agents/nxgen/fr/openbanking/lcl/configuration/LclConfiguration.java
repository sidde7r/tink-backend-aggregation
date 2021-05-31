package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration;

import lombok.Data;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@Data
public class LclConfiguration implements ClientConfiguration {
    @Secret private String clientId;
    @Secret private String qsealcKeyId;
}
