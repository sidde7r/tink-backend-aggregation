package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NatureEntity {

    private String code;

    @JsonProperty("libelleLong")
    private String longLibel;

    @JsonProperty("indicateurMasquage")
    private String hidingIndicator;

    @JsonProperty("libelleCourt")
    private String shortLabel;

    @JsonProperty("libelleMoyen")
    private String middleWord;

    String getLabel() {
        return middleWord;
    }
}
