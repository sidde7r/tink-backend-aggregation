package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAvailability {
    // The userPresent flag indicates whether or not the user is present at the time of the
    // operation. A non-present user means it is a scheduled background refresh, but a present
    // user doesn't necessarily mean we can interact with the user. Imagine an app that,
    // automatically, refreshes all credentials upon the user entering the app. This doesn't mean
    // that the user is ready ("available") for interaction. For knowing if the user is avialable
    // for interaction, use the flag userAvailableForInteraction.
    // This flag can be used when priotizing operations; we typically want to complete the
    // operation as quickly as possible if the user is present.
    private boolean userPresent;
    // Indicates whether or not we can interact with the user (e.g. SCA through any supplemental
    // information flow)
    private boolean userAvailableForInteraction;
    private String originatingUserIp;

    public String getOriginatingUserIp() {
        return originatingUserIp;
    }

    public void setOriginatingUserIp(String originatingUserIp) {
        this.originatingUserIp = originatingUserIp;
    }

    public boolean isUserPresent() {
        return userPresent;
    }

    public void setUserPresent(boolean userPresent) {
        this.userPresent = userPresent;
    }

    public boolean isUserAvailableForInteraction() {
        return userAvailableForInteraction;
    }

    public void setUserAvailableForInteraction(boolean userAvailableForInteraction) {
        this.userAvailableForInteraction = userAvailableForInteraction;
    }
}
