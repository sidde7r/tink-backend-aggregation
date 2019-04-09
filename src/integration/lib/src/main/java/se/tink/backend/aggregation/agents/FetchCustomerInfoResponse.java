package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.libraries.customerinfo.CustomerInfo;

public final class FetchCustomerInfoResponse {
    private CustomerInfo customerInfo;

    private FetchCustomerInfoResponse() {}

    public FetchCustomerInfoResponse(final CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    // TODO Delete this
    public static FetchCustomerInfoResponse empty() {
        return new FetchCustomerInfoResponse();
    }

    // TODO Return value should be CustomerInfo
    public Optional<CustomerInfo> getCustomerInfo() {
        return Optional.ofNullable(customerInfo);
    }
}
