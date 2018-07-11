package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RootModel {
    private ErrorEntity error;
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }

    public ErrorEntity getError() {
        return error;
    }
}
