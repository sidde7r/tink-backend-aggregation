package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StorageAreaEntity {

    @JsonProperty("iban")
    private Object iban;

    @JsonProperty("codigoPais")
    private Object codigoPais;

    @JsonProperty("dcIban")
    private Object dcIban;

    @JsonProperty("numeroCuenta")
    private String accountNumber;

    @JsonProperty("entidad")
    private String entity;

    @JsonProperty("oficina")
    private String office;

    private String dc;

    @JsonProperty("longitudModalidad")
    private int lengthModality;

    @JsonProperty("longitudCuenta")
    private int accountLength;
}
