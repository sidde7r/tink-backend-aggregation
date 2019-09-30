package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RootModel {
    private Object error;
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }

    public Object getError() {
        return error;
    }
}
