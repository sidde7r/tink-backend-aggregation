package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoryEntity {
    private String name;
    private String group;
    private String id;
    private String amount;

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }
}
