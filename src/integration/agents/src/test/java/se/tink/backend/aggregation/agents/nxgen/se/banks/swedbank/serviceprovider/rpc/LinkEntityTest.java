package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LinkEntityTest {

    @Test
    public void shouldReturnTrueIfUriIsValid() {
        LinkEntity linkEntity =
                SerializationUtils.deserializeFromString(
                        "{\"method\": \"GET\", \"uri\": \"/this/is/fakeurl\"}", LinkEntity.class);

        boolean result = linkEntity.isValid();

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfMethodUnknown() {
        LinkEntity linkEntity =
                SerializationUtils.deserializeFromString(
                        "{\"\": \"GET\", \"uri\": \"/this/is/fakeurl\"}", LinkEntity.class);

        boolean result = linkEntity.isValid();

        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseIfUriIsEmpty() {
        LinkEntity linkEntity =
                SerializationUtils.deserializeFromString(
                        "{\"\": \"GET\", \"uri\": \"\"}", LinkEntity.class);

        boolean result = linkEntity.isValid();

        assertFalse(result);
    }
}
