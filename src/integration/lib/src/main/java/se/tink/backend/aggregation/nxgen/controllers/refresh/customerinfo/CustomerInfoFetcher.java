package se.tink.backend.aggregation.nxgen.controllers.refresh.customerinfo;

import se.tink.libraries.customerinfo.CustomerInfo;

public interface CustomerInfoFetcher {

    CustomerInfo fetchCustomerInfo();
}
