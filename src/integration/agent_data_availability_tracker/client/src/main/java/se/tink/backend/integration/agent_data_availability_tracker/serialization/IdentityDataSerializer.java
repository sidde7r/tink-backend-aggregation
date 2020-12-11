package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class IdentityDataSerializer extends TrackingMapSerializer {

    public static final String IDENTITY_DATA = "IdentityData";
    private final IdentityData identityData;

    public IdentityDataSerializer(IdentityData identityData) {
        super(IDENTITY_DATA);
        this.identityData = identityData;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {

        listBuilder
                .putRedacted("name", identityData.getName())
                .putRedacted("ssn", identityData.getSsn())
                .putRedacted("dateOfBirth", identityData.getDateOfBirth());

        return listBuilder.build();
    }
}
