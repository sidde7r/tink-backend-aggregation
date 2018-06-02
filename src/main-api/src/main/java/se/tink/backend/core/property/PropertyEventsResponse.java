package se.tink.backend.core.property;

import java.util.List;

public class PropertyEventsResponse {
    private List<PropertyEvent> propertyEvents;

    public PropertyEventsResponse(List<PropertyEvent> propertyEvents) {
        this.propertyEvents = propertyEvents;
    }

    public List<PropertyEvent> getPropertyEvents() {
        return propertyEvents;
    }

    public void setPropertyEvents(List<PropertyEvent> propertyEvents) {
        this.propertyEvents = propertyEvents;
    }
}


