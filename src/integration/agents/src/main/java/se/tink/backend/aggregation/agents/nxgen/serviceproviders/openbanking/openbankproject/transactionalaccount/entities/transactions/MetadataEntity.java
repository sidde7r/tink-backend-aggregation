package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetadataEntity {

    private String publicAlias;
    private String privateAlias;
    private String moreInfo;
    private String uRL;
    private String imageURL;
    private String openCorporatesURL;
    private CorporateLocationEntity corporate_location;
    private PhysicalLocationEntity physicalLocation;

    public CorporateLocationEntity getCorporate_location() {
        return corporate_location;
    }

    public void setCorporate_location(CorporateLocationEntity corporate_location) {
        this.corporate_location = corporate_location;
    }

    public String getPublicAlias() {
        return publicAlias;
    }

    public String getPrivateAlias() {
        return privateAlias;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public String getuRL() {
        return uRL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getOpenCorporatesURL() {
        return openCorporatesURL;
    }

    public PhysicalLocationEntity getPhysicalLocation() {
        return physicalLocation;
    }
}
