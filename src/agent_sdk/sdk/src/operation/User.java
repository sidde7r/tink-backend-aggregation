package se.tink.agent.sdk.operation;

import javax.annotation.Nullable;

public interface User {
    /**
     * The return value of this method indicates whether the user is present at the time of the
     * operation. A non-present user means it is a scheduled background refresh, but a present user
     * doesn't necessarily mean we can interact with the user. Imagine an app that, automatically,
     * refreshes all credentials upon the user entering the app. This doesn't mean that the user is
     * ready ("available") for interaction. For knowing if the user is available for interaction,
     * use the method {@link #isAvailableForInteraction()}. This flag can be used when prioritizing
     * operations; we typically want to complete the operation as quickly as possible if the user is
     * present.
     *
     * @return true if the user is present
     */
    boolean isPresent();

    /**
     * Indicates whether we can interact with the user (e.g. SCA through any supplemental
     * information flow)
     *
     * @return true if we are allowed to interact with the user
     */
    boolean isAvailableForInteraction();

    /**
     * @return the user's IP address if {@link #isPresent()} returns true, otherwise it will return
     *     null. If the user's IP address is not known (not given to us) this method will default to
     *     `127.0.0.1`.
     */
    @Nullable
    String getIpAddress();

    /** @return the user's locale */
    String getLocale();
}
