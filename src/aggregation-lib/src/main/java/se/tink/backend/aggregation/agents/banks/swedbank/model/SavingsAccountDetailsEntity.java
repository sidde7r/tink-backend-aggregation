package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsAccountDetailsEntity {
    private LinksEntity links;

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
