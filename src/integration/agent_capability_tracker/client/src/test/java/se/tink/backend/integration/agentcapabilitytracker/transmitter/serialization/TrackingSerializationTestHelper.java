package se.tink.backend.integration.agentcapabilitytracker.transmitter.serialization;

import static se.tink.backend.integration.agentcapabilitytracker.transmitter.serialization.TrackingList.Builder.VALUE_NOT_LISTED;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.stream.Collectors;

public class TrackingSerializationTestHelper {

    static boolean isAllUnlisted(
            final ImmutableSet<String> expectedUnlistedFieldNames, final List<FieldEntry> entries) {

        return entries.stream()
                .filter(e -> expectedUnlistedFieldNames.contains(e.getName()))
                .allMatch(e -> VALUE_NOT_LISTED.equals(e.getValue()));
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
