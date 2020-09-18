package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import java.time.LocalDate;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class DemobankIdentityDataFetcher implements IdentityDataFetcher {

    @Override
    public IdentityData fetchIdentityData() {
        return SeIdentityData.builder()
                .setSsn("198511115273")
                .setFullName("John Doe")
                .setDateOfBirth(LocalDate.of(11, 11, 11))
                .build();
    }
}
