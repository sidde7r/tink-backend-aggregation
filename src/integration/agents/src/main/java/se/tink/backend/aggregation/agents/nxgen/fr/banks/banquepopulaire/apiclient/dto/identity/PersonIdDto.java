package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class PersonIdDto {

    private CiviliteDto civilite;

    private String nomMarital;

    @JsonProperty("nomPatronymique")
    private String surname;

    @JsonProperty("prenom")
    private String firstName;
}
