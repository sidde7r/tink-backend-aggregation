package se.tink.backend.core.property;

import io.protostuff.Tag;

public class UpdatePropertyRequest {
    @Tag(1)
    private Property property;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
