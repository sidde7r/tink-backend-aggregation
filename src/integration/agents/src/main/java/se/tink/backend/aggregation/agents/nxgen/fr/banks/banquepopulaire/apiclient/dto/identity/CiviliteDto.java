package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class CiviliteDto {

    private String code;

    private String label;

    private String type;
}
