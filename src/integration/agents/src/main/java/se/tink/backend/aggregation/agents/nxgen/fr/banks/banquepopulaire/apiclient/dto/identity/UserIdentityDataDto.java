package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class UserIdentityDataDto {

    private String codeEtablissement;

    private String dateDerniereConnexion;

    private IdAbonneDto idAbonne;

    private IdClientDto idClient;

    @JsonProperty("identitePersonnePhysique")
    private PersonIdDto personId;
}
