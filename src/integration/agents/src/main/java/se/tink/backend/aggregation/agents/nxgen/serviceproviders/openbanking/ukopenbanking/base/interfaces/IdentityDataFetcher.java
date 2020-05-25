package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import java.util.Optional;
import se.tink.libraries.identitydata.IdentityData;

public interface IdentityDataFetcher {

    Optional<IdentityData> fetchIdentityData();
}
