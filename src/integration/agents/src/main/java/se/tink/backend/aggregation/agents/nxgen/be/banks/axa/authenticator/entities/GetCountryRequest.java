package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GetCountryRequest {

    private GetCountryInput input;

    public GetCountryInput getInput() {
        return input;
    }

    public void setInput(GetCountryInput input) {
        this.input = input;
    }
}
