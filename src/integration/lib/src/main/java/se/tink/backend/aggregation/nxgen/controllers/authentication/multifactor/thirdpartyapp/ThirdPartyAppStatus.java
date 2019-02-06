package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

public enum ThirdPartyAppStatus {
    DONE,
    WAITING,
    CANCELLED,
    TIMED_OUT,
    ALREADY_IN_PROGRESS,
    UNKNOWN
}
