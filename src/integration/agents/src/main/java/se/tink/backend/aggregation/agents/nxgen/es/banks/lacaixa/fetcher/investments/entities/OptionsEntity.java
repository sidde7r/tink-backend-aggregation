package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OptionsEntity {
    private boolean permiteComprar;
    private boolean permiteVender;
    private boolean permiteTraspasar;
    private boolean permiteOrdenesPendientes;
    private boolean permiteVerOperaciones;
    private boolean permiteComparar;
}
