package se.tink.backend.aggregation.storage.logs;

public enum SaveLogsStatus {
    SAVED,
    SKIPPED,
    NO_AVAILABLE_STORAGE,
    NO_LOGS,
    EMPTY_LOGS,
    ERROR
}
