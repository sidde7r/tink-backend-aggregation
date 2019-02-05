package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTypeEntity {
    private String background;
    private String type;
    private String subtype;
    private String textColor;
    private String iconColor;
    private String logo;

    public String getBackground() {
        return background;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getIconColor() {
        return iconColor;
    }

    public String getLogo() {
        return logo;
    }
}
