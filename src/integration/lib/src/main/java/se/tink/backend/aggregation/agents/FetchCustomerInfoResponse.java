package se.tink.backend.aggregation.agents;

import se.tink.libraries.identitydata.IdentityData;

public final class FetchCustomerInfoResponse {
    private final IdentityData customerInfo;

    public FetchCustomerInfoResponse(final IdentityData customerInfo) {
        this.customerInfo = customerInfo;
    }

    public IdentityData getCustomerInfo() {
        return customerInfo;
    }
}
