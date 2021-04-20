package se.tink.libraries.credentials.service;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public enum RefreshableSegment {
    BUSINESS,
    PERSONAL,
    UNDETERMINED;

    public static final Set<RefreshableSegment> REFRESHABLE_SEGMENTS_ALL =
            ImmutableSet.<RefreshableSegment>builder().add(RefreshableSegment.values()).build();
}
