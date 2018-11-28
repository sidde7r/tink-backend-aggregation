package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerData {
    private String firstname;
    private String clientType;
    private String zoomitId;
    private String name;
    private String language;
    private int title;
    private String blueFlag;
    private String birthDate;

    public String getFirstname() {
        return firstname;
    }

    public String getClientType() {
        return clientType;
    }

    public String getZoomitId() {
        return zoomitId;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public int getTitle() {
        return title;
    }

    public String getBlueFlag() {
        return blueFlag;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
