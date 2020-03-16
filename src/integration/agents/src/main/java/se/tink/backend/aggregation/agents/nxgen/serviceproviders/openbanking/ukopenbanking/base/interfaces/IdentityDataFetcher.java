package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface IdentityDataFetcher {

    IdentityDataEntity fetchUserDetails(URL identityDataEndpointURL);
}
