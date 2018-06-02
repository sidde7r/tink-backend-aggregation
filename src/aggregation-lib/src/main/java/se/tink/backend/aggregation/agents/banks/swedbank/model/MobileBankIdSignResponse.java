package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankIdSignResponse extends AbstractResponse {
    private String signingData;
    private String signingText;
    private String signingStatus;
    private LinksEntity links;

    public String getSigningData() {
        return signingData;
    }

    public void setSigningData(String signingData) {
        this.signingData = signingData;
    }

    public String getSigningText() {
        return signingText;
    }

    public void setSigningText(String signingText) {
        this.signingText = signingText;
    }

    public String getSigningStatus() {
        return signingStatus;
    }

    public void setSigningStatus(String signingStatus) {
        this.signingStatus = signingStatus;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

}
