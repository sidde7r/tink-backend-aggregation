package se.tink.backend.categorization.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTrainingResponse implements Cloneable {
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
