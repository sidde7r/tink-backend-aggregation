package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ViewEntity {
    private String title;
    private String type;
    private String navigationType;
    private String layout;
    private ConfigurationEntity configurations;
    private SectionEntity sections;

    public SectionEntity getSections() {
        return sections;
    }
}
