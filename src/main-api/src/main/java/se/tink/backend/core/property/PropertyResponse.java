package se.tink.backend.core.property;

import io.protostuff.Tag;

public class PropertyResponse {
    @Tag(1)
    private Property property;

    public PropertyResponse() {

    }

    public PropertyResponse(Property property) {
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
