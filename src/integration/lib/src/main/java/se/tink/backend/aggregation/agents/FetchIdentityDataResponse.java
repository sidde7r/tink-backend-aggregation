package se.tink.backend.aggregation.agents;

import se.tink.libraries.identitydata.IdentityData;

public final class FetchIdentityDataResponse {
    private final IdentityData customerInfo;

    public FetchIdentityDataResponse(final IdentityData customerInfo) {
        this.customerInfo = customerInfo;
    }

    public IdentityData getCustomerInfo() {
        return customerInfo;
    }
}
