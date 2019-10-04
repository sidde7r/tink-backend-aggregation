package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

/**
 * Class designed to pass status of an external app.
 *
 * @param <T>
 */
public interface ExternalThirdPartyAppResponse<T> {
    /**
     * Returns an app based on a bank response using {@link ThirdPartyAppStatus}
     *
     * @return
     */
    ThirdPartyAppStatus getStatus();

    /**
     * A value by which the request is associated to the bank status
     *
     * @return
     */
    T getReference();
}
