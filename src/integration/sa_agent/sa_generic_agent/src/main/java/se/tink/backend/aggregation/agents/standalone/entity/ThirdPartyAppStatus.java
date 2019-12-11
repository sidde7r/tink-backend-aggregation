package se.tink.backend.aggregation.agents.standalone.entity;

public enum ThirdPartyAppStatus {
    DONE,
    WAITING,
    CANCELLED,
    TIMED_OUT,
    ALREADY_IN_PROGRESS,
    AUTHENTICATION_ERROR,
    UNKNOWN
}
