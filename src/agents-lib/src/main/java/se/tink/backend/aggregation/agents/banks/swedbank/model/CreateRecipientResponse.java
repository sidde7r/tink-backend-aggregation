package se.tink.backend.aggregation.agents.banks.swedbank.model;

public class CreateRecipientResponse extends AbstractResponse {
    private LinksEntity links;

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
