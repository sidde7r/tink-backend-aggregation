package se.tink.backend.aggregation.provider.configuration.storage.models;

public enum ProviderStatuses {
    ENABLED, // Provider is refreshed and shown in providers list.
    OBSOLETE, // Provider is refreshed but not shown in providers list.
    TEMPORARY_DISABLED, DISABLED  // Provider is not refreshed and not shown in providers list.
}
