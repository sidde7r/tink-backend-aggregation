package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.FieldEntry;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList.Builder;

public class TrackingSerializationTestHelper {

    static boolean isAllUnlisted(
            final ImmutableSet<String> expectedUnlistedFieldNames, final List<FieldEntry> entries) {

        return entries.stream()
                .filter(e -> expectedUnlistedFieldNames.contains(e.getName()))
                .allMatch(e -> Builder.VALUE_NOT_LISTED.equals(e.getValue()));
    }

    static boolean hasFieldWithValue(
            final String fieldKey, final String expectedValue, final List<FieldEntry> entries) {

        return entries.stream()
                .filter(e -> fieldKey.equals(e.getName()))
                .map(FieldEntry::getValue)
                .anyMatch(expectedValue::equals);
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
