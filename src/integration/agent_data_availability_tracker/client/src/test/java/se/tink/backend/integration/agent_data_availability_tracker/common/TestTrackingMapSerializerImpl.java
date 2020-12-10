package se.tink.backend.integration.agent_data_availability_tracker.common;

import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList.Builder;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class TestTrackingMapSerializerImpl extends TrackingMapSerializer {

    private final String fieldName;
    private final String fieldValue;

    public TestTrackingMapSerializerImpl(String entityKey, String fieldName, String fieldValue) {
        super(entityKey);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    protected TrackingList populateTrackingMap(Builder list) {

        return list.putListed(fieldName, fieldValue).build();
    }
}
