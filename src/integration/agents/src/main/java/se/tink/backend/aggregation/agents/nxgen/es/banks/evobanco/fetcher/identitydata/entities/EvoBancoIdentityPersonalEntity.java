package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class EvoBancoIdentityPersonalEntity {

    @JsonProperty("idExterno")
    private String documentId;

    @JsonProperty("nombreCliente")
    private String name;

    @JsonProperty("apellido1Cliente")
    private String firstSurname;

    @JsonProperty("apellido2Cliente")
    private String secondSurname;

    @JsonProperty("fechaNacimiento")
    private String dateOfBirth;

    private String mail;

    @JsonProperty("telefonoMovil")
    private String phone;

    @JsonProperty("codigoIdExterno")
    private String codeDocumentId;

    @JsonProperty("sexo")
    private String sex;

    @JsonProperty("fechaCaducidadDocumento")
    private String dateOfDocumentExpiration;

    @JsonProperty("paisNacionalidad")
    private String codeNationality;

    @JsonProperty("paisResidencia")
    private String codeResidence;
}
