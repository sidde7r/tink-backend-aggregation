package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowYourCustomer {
    @JsonProperty("MustUpdate")
    private boolean mustUpdate;

    @JsonProperty("PostponesLeft")
    private int postponesLeft;

    public boolean isMustUpdate() {
        return mustUpdate;
    }

    public void setMustUpdate(boolean mustUpdate) {
        this.mustUpdate = mustUpdate;
    }

    public int getPostponesLeft() {
        return postponesLeft;
    }

    public void setPostponesLeft(int postponesLeft) {
        this.postponesLeft = postponesLeft;
    }
}
