package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NumeroContratoEntity {
    private String area;
    private String digitosControl;
    private String entidad;
    private int longitudContrato;
    private int longitudModalidad;
    private String modalidad;
    private String numeroContrato;
    private String oficina;
    private String refContrato;
}
