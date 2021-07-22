package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FiduciaProviderInfo {
    private String serverUrl;
    private String bic;
}
