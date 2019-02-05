package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineItemsEntity {
    private String date;
    private List<SubItemsEntity> subItems;

    public String getDate() {
        return date;
    }

    public List<SubItemsEntity> getSubItems() {
        return subItems;
    }
}
