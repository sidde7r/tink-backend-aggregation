package se.tink.backend.aggregation.storage.logs;

public enum SaveLogsStatus {
    SAVED,
    SKIPPED,
    STORAGE_DISABLED,
    NO_LOGS,
    EMPTY_LOGS,
    ERROR
}
