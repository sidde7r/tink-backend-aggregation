package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientEntity {
    private IdentifierEntity idClient;
    private String descriptionClient;

    public IdentifierEntity getIdClient() {
        return idClient;
    }

    public String getDescriptionClient() {
        return descriptionClient;
    }
}
