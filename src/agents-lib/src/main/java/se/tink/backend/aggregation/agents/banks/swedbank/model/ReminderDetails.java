package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderDetails {

    private int count;
    private LinksEntity links;

    public int getCount() {
        return count;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
