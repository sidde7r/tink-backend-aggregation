package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataEntity {
    @JsonProperty("tipoPersona")
    private String personType;

    @JsonProperty("nombreRazonSocial")
    private String fullName;

    @JsonProperty("claseDocumento")
    private String documentType;

    @JsonProperty("documentoCliente")
    private String clientDocument;

    @JsonProperty("numeroTelefonoLargo")
    private String phoneNumber;

    @JsonProperty("fechaNacimiento")
    private String dateOfBirth;

    @JsonProperty("nombreCliente")
    private String firstName;

    @JsonProperty("primerApellido")
    private String firstSurName;

    @JsonProperty("segundoApellido")
    private String secondSurName;

    @JsonProperty("identificadorCliente")
    private String clientIdNumber;

    @JsonProperty("datosDomicilio")
    private AddressDataEntity addressData;

    public String getPersonType() {
        return personType;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getClientDocument() {
        return clientDocument;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFirstSurName() {
        return firstSurName;
    }

    public String getSecondSurName() {
        return secondSurName;
    }

    public String getClientIdNumber() {
        return clientIdNumber;
    }

    public AddressDataEntity getAddressData() {
        return addressData;
    }
}
