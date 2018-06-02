package se.tink.backend.core.property;

import io.protostuff.Tag;
import java.util.List;

public class ListPropertiesResponse {
    @Tag(1)
    private List<Property> properties;

    public ListPropertiesResponse () {

    }

    public ListPropertiesResponse(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}
