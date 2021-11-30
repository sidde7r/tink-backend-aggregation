package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

public enum ThirdPartyAppStatus {
    // The user completed the authentication via the third party app.
    DONE,

    // The third party app startup parameters (i.e. [se] autostart token) has expired and must be
    // reinitialized.
    REINITIATE_APP,

    // The user has not yet completed the authentication.
    PENDING,

    // The user has no registered third party app.
    NO_CLIENT,

    // In case the third party app is not allowed to have simultaneous authentications in progress
    // at once both will be aborted.
    ALREADY_IN_PROGRESS,

    // The user cancelled the authentication in the third party app.
    CANCELLED,

    // The user did not complete the authentication in the third party app in time.
    TIMED_OUT,

    // Unknown cause for failure.
    UNKNOWN_FAILURE,
}
