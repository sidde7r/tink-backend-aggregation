package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

public interface ThirdPartyAppResponse<T> {
    ThirdPartyAppStatus getStatus();
    T getReference();
}
