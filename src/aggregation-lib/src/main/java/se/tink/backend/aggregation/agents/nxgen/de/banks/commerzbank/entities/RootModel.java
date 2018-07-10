package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RootModel {
    // `error` is null - cannot define it!
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }
}
