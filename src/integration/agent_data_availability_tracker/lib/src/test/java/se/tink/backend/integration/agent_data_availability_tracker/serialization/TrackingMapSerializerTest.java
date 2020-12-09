package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.teststubs.TestTrackingMapSerializerImpl;

public class TrackingMapSerializerTest {

    private static final String ENTITY_A = "Entity_A";
    private static final String ENTITY_B = "Entity_B";
    private static final String ENTITY_A_BEES_FIELD = "bees";
    private static final String FIELD_A = "field_in_A";
    private static final String FIELD_B = "field_in_B";

    private static final String FIELD_A_KEY = ENTITY_A + "." + FIELD_A;
    private static final String FIELD_B_KEY =
            ENTITY_A + "." + ENTITY_A_BEES_FIELD + "." + ENTITY_B + "." + FIELD_B;

    @Test
    public void ensureNestedEntities_endUpWhereExpected() {

        TestTrackingMapSerializerImpl entityA =
                new TestTrackingMapSerializerImpl(ENTITY_A, FIELD_A, "A");
        TestTrackingMapSerializerImpl entityB =
                new TestTrackingMapSerializerImpl(ENTITY_B, FIELD_B, "B");
        TestTrackingMapSerializerImpl entityBB =
                new TestTrackingMapSerializerImpl(ENTITY_B, FIELD_B, "BB");
        entityA.addChild(ENTITY_A_BEES_FIELD, entityB);
        entityA.addChild(ENTITY_A_BEES_FIELD, entityBB);

        List<FieldEntry> entries = entityA.buildList();

        Assert.assertTrue(hasFieldWithValues(FIELD_A_KEY, ImmutableSet.of("A"), entries));
        Assert.assertTrue(hasFieldWithValues(FIELD_B_KEY, ImmutableSet.of("B", "BB"), entries));
    }

    static boolean hasFieldWithValues(
            final String fieldKey,
            final ImmutableSet<String> expectedValues,
            final List<FieldEntry> entries) {

        return entries.stream()
                .filter(e -> fieldKey.equals(e.getName()))
                .map(FieldEntry::getValue)
                .collect(Collectors.toSet())
                .equals(expectedValues);
    }
}
