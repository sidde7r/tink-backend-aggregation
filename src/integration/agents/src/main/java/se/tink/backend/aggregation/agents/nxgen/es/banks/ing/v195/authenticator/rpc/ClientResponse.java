package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.entity.Preferences;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ClientResponse {

    private String id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String documentNumber;
    private int documentType;
    private String birthDate;
    private String createClientDate;
    private int personId;
    private String typeDescription;
    private String typeCode;
    private String genderCode;
    private String genderDesc;
    private String nationality;
    private String nativeCountry;
    private String selfResFiscal;
    private Preferences preferences;

    public String getId() {
        return id;
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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public int getDocumentType() {
        return documentType;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getCreateClientDate() {
        return createClientDate;
    }

    public int getPersonId() {
        return personId;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getGenderCode() {
        return genderCode;
    }

    public String getGenderDesc() {
        return genderDesc;
    }

    public String getNationality() {
        return nationality;
    }

    public String getNativeCountry() {
        return nativeCountry;
    }

    public String getSelfResFiscal() {
        return selfResFiscal;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
