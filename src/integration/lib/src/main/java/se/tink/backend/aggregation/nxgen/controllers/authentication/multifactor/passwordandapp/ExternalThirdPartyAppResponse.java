package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public interface ExternalThirdPartyAppResponse<T> {
    ThirdPartyAppStatus getStatus();

    T getReference();
}
