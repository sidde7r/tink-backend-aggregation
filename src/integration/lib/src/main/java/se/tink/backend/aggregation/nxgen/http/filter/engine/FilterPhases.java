package se.tink.backend.aggregation.nxgen.http.filter.engine;

import com.google.common.collect.ImmutableList;

public enum FilterPhases {
    REQUEST_HANDLE,
    PRE_PROCESS,
    CUSTOM,
    PRE_SECURITY,
    SECURITY,
    POST_SECURITY,
    SEND;

    public static ImmutableList<FilterPhases> asDefaultOrderedList() {
        return ImmutableList.of(
                REQUEST_HANDLE, PRE_PROCESS, CUSTOM, PRE_SECURITY, SECURITY, POST_SECURITY, SEND);
    }
}
