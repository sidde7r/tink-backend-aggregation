package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class GeneralConfigurationResponseDto {

    private BrandResourceDto brand;
    private List<FusionResourceDto> fusion;
}
