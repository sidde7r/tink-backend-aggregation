package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class FusionResourceDto {

    private String idSource;

    private String idCible;

    private String baseUrl;
}
