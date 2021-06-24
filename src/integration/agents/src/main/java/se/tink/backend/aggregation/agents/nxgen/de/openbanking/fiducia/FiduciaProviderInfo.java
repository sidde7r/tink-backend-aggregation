package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FiduciaProviderInfo {
    private String serverUrl;
    private String bic;
}
