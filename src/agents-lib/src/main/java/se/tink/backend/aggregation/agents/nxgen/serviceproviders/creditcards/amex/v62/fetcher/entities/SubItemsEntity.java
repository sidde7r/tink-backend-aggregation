package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubItemsEntity {
    private String type;
    private String id;
    private String date;
    private String title;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPending() {
        return type.equals(AmericanExpressV62Constants.Tags.IS_PENDING);
    }
}
