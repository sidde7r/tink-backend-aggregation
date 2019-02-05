package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WidgetEventInformation {

    private String widgetType;
    private String widgetId;
    private String eventType;
    private Map<String, Object> attributes;

    public String getWidgetType() {
        return widgetType;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static Builder builder(String widgetId) {
        return new Builder(widgetId);
    }

    public static class Builder {
        private WidgetEventInformation widget;

        public Builder(String widgetId) {
            widget = new WidgetEventInformation();
            widget.widgetId = widgetId;
            widget.attributes = new HashMap<>();
        }

        public Builder setWidgetType(String widgetType) {
            widget.widgetType = widgetType;
            return this;
        }

        public Builder setEventType(String eventType) {
            widget.eventType = eventType;
            return this;
        }

        public Builder setAttribute(String key, String value) {
            widget.attributes.put(key, value);
            return this;
        }

        public Builder setTextAttribute(String value) {
            return setAttribute("text", value);
        }

        public Builder setActiveElement(String activeElement) {
            return setAttribute("activeElement", activeElement);
        }

        public WidgetEventInformation build() {
            return widget;
        }
    }

    public static WidgetEventInformation newInputValueChangedWidgetEvent(String widgetId, String value) {
        return WidgetEventInformation.builder(widgetId)
                .setWidgetType("M_Input")
                .setEventType("valueChanged")
                .setTextAttribute(value).build();
    }

    public static WidgetEventInformation newButtonClickedWidgetEvent(String widgetId) {
        return WidgetEventInformation.builder(widgetId)
                .setWidgetType("M_Button")
                .setEventType("clicked").build();
    }

    public static WidgetEventInformation newRepeaterValueChangedWidgetEvent(String widgetId, String activeElement) {
        return WidgetEventInformation.builder(widgetId)
                .setWidgetType("Repeater")
                .setEventType("valueChanged")
                .setActiveElement(activeElement).build();
    }
}
