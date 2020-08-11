package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration;

import lombok.Data;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@Data
public class LclConfiguration implements ClientConfiguration {

    private String authorizeUrl;

    private String baseUrl;

    private String qsealcKeyId;

    private String clientId;
}
