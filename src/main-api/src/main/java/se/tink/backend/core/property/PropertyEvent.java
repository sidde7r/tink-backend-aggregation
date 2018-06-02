package se.tink.backend.core.property;

import java.util.Date;
import java.util.Map;

// This class could probably remade to extend a generic Event class (because this shares the same properties as AccountEvent, may be more intuitive if this was just a subclass instead
public class PropertyEvent {
    private String propertyId;
    private Date timestamp;
    private Type type;
    private String title;

    private Map<String, Object> properties;

    public PropertyEvent(String propertyId, Date timestamp, Type type) {
        this.propertyId = propertyId;
        this.timestamp = timestamp;
        this.type = type;

        switch(type) {
            case VALUATION_INCREASE:
                this.title = "The property valuation has increased";
                break;
            case VALUATION_DECREASE:
                this.title = "The property valuation has decreased";
                break;
            case INFO:
                break;
        }
    }

    public String getPropertyId() {
        return propertyId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (this.type != Type.INFO) {
            throw new IllegalArgumentException();
        }
        this.title = title;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        switch(this.type) {
            case VALUATION_INCREASE:
            case VALUATION_DECREASE:
                if (properties.get("currentValuation") == null || properties.get("change") == null || properties.get("percentChange") == null) {
                    throw new IllegalArgumentException();
                }
                break;
            case INFO:
                if (properties.get("temporal") == null) {
                    throw new IllegalArgumentException();
                }
                break;
        }
        this.properties = properties;
    }

    public enum Type {
        INFO,
        VALUATION_INCREASE,
        VALUATION_DECREASE
    }
}
