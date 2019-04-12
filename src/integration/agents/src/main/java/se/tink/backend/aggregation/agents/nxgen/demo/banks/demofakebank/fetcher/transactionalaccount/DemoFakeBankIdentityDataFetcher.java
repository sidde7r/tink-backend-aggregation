package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount;

import java.time.LocalDate;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class DemoFakeBankIdentityDataFetcher implements IdentityDataFetcher {
    @Override
    public IdentityData fetchIdentityData() {

        EsIdentityData.EsIdentityDataBuilder builder = EsIdentityData.builder();

        return builder.addFirstNameElement("user")
                .addSurnameElement("userson")
                .setDateOfBirth(LocalDate.of(1970, 1, 1))
                .build();
    }
}
