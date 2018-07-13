package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoalsBaseInfoResponse extends AbstractResponse {
    private SavingsGoalsBaseInfoMetadataEntity metadata;
    private LinksEntity links;

    public SavingsGoalsBaseInfoMetadataEntity getMetadata() {
        return metadata;
    }

    public void setMetadata(SavingsGoalsBaseInfoMetadataEntity metadata) {
        this.metadata = metadata;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

}
