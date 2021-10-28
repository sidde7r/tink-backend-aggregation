package se.tink.backend.aggregation.storage.logs;

public enum SaveLogsStatus {
    SAVED,
    STORAGE_DISABLED,
    NO_LOGGER,
    LOGS_SHOULD_NOT_BE_STORED,
    NO_LOGS,
    EMPTY_LOGS,
    ERROR
}
