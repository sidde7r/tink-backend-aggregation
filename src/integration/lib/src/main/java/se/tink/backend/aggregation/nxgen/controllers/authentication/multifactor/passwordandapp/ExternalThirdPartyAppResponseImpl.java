package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

public class ExternalThirdPartyAppResponseImpl implements ExternalThirdPartyAppResponse<String> {

    private final ThirdPartyAppStatus status;
    private final String reference;

    private ExternalThirdPartyAppResponseImpl(ThirdPartyAppStatus status, String reference) {
        this.status = status;
        this.reference = reference;
    }

    public static ExternalThirdPartyAppResponseImpl create(
            ThirdPartyAppStatus status, String reference) {
        return new ExternalThirdPartyAppResponseImpl(status, reference);
    }

    public static ExternalThirdPartyAppResponseImpl create(ThirdPartyAppStatus status) {
        return new ExternalThirdPartyAppResponseImpl(status, null);
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
