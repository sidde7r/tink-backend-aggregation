package se.tink.backend.aggregation.agents;

import se.tink.libraries.customerinfo.CustomerInfo;

public final class FetchCustomerInfoResponse {
    private final CustomerInfo customerInfo;

    public FetchCustomerInfoResponse(final CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }
}
