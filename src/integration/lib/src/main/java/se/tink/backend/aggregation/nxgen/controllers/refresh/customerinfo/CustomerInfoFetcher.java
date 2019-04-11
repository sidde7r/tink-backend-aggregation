package se.tink.backend.aggregation.nxgen.controllers.refresh.customerinfo;

import se.tink.libraries.identitydata.IdentityData;

public interface CustomerInfoFetcher {

    IdentityData fetchCustomerInfo();
}
