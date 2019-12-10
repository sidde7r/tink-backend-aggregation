package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetsResponse extends HeaderResponse {

    private List<AssetJarsDto> assetJars;
    private TypeValuePair hasAgentInsurances;
    private TypeValuePair hasCeraShares;
    private TypeValuePair hasPowerOfAttorney;

    public List<AssetJarsDto> getAssetJars() {
        return assetJars;
    }

    public TypeValuePair getHasAgentInsurances() {
        return hasAgentInsurances;
    }

    public TypeValuePair getHasCeraShares() {
        return hasCeraShares;
    }

    public TypeValuePair getHasPowerOfAttorney() {
        return hasPowerOfAttorney;
    }
}
