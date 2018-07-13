package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CountEntity {
    private String count;

    public String getCount() {
        return count;
    }
}
