package se.tink.backend.integration.agent_data_availability_tracker.client.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;

public class IdentityDataSerializerTest {

    private static final String VALUE_NOT_LISTED = TrackingList.Builder.VALUE_NOT_LISTED;
    private static final String SECRET_VALUE = "SecretValue";

    @Test
    public void ensureSecretAccountFields_areRedacted() {

        ImmutableSet<String> secretFieldKeys =
                ImmutableSet.<String>builder()
                        .add("IDENTITY_DATA.name", "IDENTITY_DATA.ssn", "IDENTITY_DATA.dateOfBirth")
                        .build();

        IdentityData identityData = new IdentityData();
        identityData.setSsn(SECRET_VALUE);
        identityData.setDateOfBirth(SECRET_VALUE);
        identityData.setName(SECRET_VALUE);

        List<FieldEntry> entries = new IdentityDataSerializer(identityData).buildList();

        Assert.assertTrue(
                "Failed: all entries in secretFieldKeys set is unlisted",
                TrackingSerializationTestHelper.isAllUnlisted(secretFieldKeys, entries));
    }
}
