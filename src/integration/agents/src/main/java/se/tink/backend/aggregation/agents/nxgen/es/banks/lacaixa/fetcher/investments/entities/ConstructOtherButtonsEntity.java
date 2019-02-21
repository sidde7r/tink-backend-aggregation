package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConstructOtherButtonsEntity {
    private boolean verCarterasGestionadas;
    private boolean verBancaPrivada;
    private boolean verBancaPersonal;
}
