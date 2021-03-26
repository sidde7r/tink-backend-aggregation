package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EvoBancoIdentityEntity {

    @JsonProperty("nombre")
    private String name;

    @JsonProperty("primerApellido")
    private String firstSurname;

    @JsonProperty("segundoApellido")
    private String secondSurname;

    private String email;

    @JsonProperty("telefono")
    private String phone;

    @JsonProperty("idExterno")
    private String documentId;

    @JsonProperty("codigoIdExterno")
    private String codeDocumentId;

    @JsonProperty("sexo")
    private String sex;

    @JsonProperty("fechaNacimiento")
    private String dateOfBirth;

    @JsonProperty("fechaCaducidadDocumento")
    private String dateOfDocumentExpiration;

    @JsonProperty("codPaisNacionalidad")
    private String codeNationality;

    @JsonProperty("codPaisResidencia")
    private String codeResidence;

    @JsonProperty("riesgoCliente")
    private String customerRisk;

    @JsonProperty("fechaUltimaModificacion")
    private String dateLastModification;

    public String getDocumentId() {
        return documentId;
    }

    public String getName() {
        return name;
    }

    public String getFirstSurname() {
        return firstSurname;
    }

    public String getSecondSurname() {
        return secondSurname;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
