package se.tink.backend.integration.agent_data_availability_tracker.common.serialization;

class ChildEntry {
    private final String fieldName;
    private final TrackingMapSerializer child;

    private ChildEntry(String fieldName, TrackingMapSerializer child) {
        this.fieldName = fieldName;
        this.child = child;
    }

    static ChildEntry of(String fieldName, TrackingMapSerializer child) {
        return new ChildEntry(fieldName, child);
    }

    TrackingList buildTrackingMap(String parentKey) {
        return child.buildTrackingList(parentKey + fieldName + ".");
    }
}
