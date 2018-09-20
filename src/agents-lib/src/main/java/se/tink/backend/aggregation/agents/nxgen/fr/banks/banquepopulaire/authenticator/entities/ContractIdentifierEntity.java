package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.IdentifierEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractIdentifierEntity extends IdentifierEntity {
    @JsonProperty("typeContrat")
    private String typeContract;
}
