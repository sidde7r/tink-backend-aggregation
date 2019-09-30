package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PhotoTanCodeEntity {
    private String verificationNumber;

    private PhotoTanCodeEntity(String photoTanCode) {
        this.verificationNumber = photoTanCode;
    }

    public static PhotoTanCodeEntity create(String photoTanCode) {
        return new PhotoTanCodeEntity(photoTanCode);
    }
}
