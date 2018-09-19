package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.BrandEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.FusionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GeneralConfigrationResponse {
    private BrandEntity brand;
    private List<FusionEntity> fusion;

    @JsonIgnore
    public Map<String, BankEntity> getBankConfiguration() {
        return brand.getBankConfiguration();
    }
}
