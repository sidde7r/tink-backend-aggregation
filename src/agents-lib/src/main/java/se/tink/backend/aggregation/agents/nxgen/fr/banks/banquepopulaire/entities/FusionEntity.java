package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FusionEntity {
    private String idSource;
    private String idCible;
    private String baseUrl;
}
