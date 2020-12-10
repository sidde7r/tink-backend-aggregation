package se.tink.backend.integration.agent_data_availability_tracker.common.serialization;

import java.util.ArrayList;
import java.util.List;

public abstract class TrackingMapSerializer {

    public static final String CONCATENATION_STRING = ".";
    private final String entityKey;
    private final List<ChildEntry> children;

    public TrackingMapSerializer(String entityKey) {
        this.entityKey = entityKey.concat(CONCATENATION_STRING);
        children = new ArrayList<>();
    }

    public void addChild(String fieldName, TrackingMapSerializer child) {
        children.add(ChildEntry.of(fieldName, child));
    }

    protected abstract TrackingList populateTrackingMap(TrackingList.Builder list);

    TrackingList buildTrackingList(String parentKey) {

        final String keyBase = parentKey + entityKey;

        // Construct and populate our map.
        TrackingList map = populateTrackingMap(TrackingList.builder(keyBase));

        // Build maps of children and merge them into our map.
        children.stream().map(child -> child.buildTrackingMap(keyBase)).forEach(map::addAll);

        return map;
    }

    public List<FieldEntry> buildList() {
        return buildTrackingList("").getFields();
    }
}
