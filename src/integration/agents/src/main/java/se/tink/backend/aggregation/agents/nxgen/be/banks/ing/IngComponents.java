package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;

@Data
@AllArgsConstructor
public class IngComponents {

    private final IngProxyApiClient ingProxyApiClient;
    private final IngDirectApiClient ingDirectApiClient;
    private final IngStorage ingStorage;
    private final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;
}
