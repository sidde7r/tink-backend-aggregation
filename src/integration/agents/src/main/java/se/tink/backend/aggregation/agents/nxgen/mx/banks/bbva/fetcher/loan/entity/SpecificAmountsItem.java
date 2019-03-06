package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SpecificAmountsItem {
    private List<AmountsItem> amounts;
    private String name;
    private String id;

    public AmountsItem getAmount() {
        return amounts.get(0);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
