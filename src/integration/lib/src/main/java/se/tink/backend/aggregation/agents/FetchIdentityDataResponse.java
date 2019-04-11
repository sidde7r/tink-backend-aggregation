package se.tink.backend.aggregation.agents;

import se.tink.libraries.identitydata.IdentityData;

public final class FetchIdentityDataResponse {
    private final IdentityData identityData;

    public FetchIdentityDataResponse(final IdentityData customerInfo) {
        this.identityData = customerInfo;
    }

    public IdentityData getIdentityData() {
        return identityData;
    }
}
