package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NotificationTokens {

    @JsonProperty("android")
    private String android;

    @JsonProperty("ios")
    private Object ios;

    public String getAndroid() {
        return android;
    }

    public Object getIos() {
        return ios;
    }
}
