package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetadataEntity {

    private String publicAlias;
    private String privateAlias;
    private String moreInfo;
    private String url;
    private String imageURL;
    private String openCorporatesUrl;
    private CorporateLocationEntity corporateLocation;

    private PhysicalLocationEntity physicalLocation;

    public CorporateLocationEntity getCorporateLocation() {
        return corporateLocation;
    }

    public void setCorporateLocation(CorporateLocationEntity corporateLocation) {
        this.corporateLocation = corporateLocation;
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

    public String getUrl() {
        return url;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getOpenCorporatesUrl() {
        return openCorporatesUrl;
    }

    public PhysicalLocationEntity getPhysicalLocation() {
        return physicalLocation;
    }
}
