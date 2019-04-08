package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

public class ThirdPartyAppResponseImpl implements ThirdPartyAppResponse<String> {

    private final ThirdPartyAppStatus status;
    private final String reference;

    private ThirdPartyAppResponseImpl(ThirdPartyAppStatus status, String reference) {
        this.status = status;
        this.reference = reference;
    }

    public static ThirdPartyAppResponse<String> create(
            ThirdPartyAppStatus status, String reference) {
        return new ThirdPartyAppResponseImpl(status, reference);
    }

    public static ThirdPartyAppResponse<String> create(ThirdPartyAppStatus status) {
        return new ThirdPartyAppResponseImpl(status, null);
    }

    @Override
    public ThirdPartyAppStatus getStatus() {
        return this.status;
    }

    @Override
    public String getReference() {
        return this.reference;
    }
}
