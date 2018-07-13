package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;

public class Segment {
    private String title;
    private List<Property> properties;

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public String getTitle() {
        return title != null ? title.toLowerCase() : null;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
