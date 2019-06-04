package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.identity;

import java.time.LocalDate;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class DemoFinancialInstitutionIdentityDataFetcher implements IdentityDataFetcher {
    @Override
    public IdentityData fetchIdentityData() {
        return EsIdentityData.builder()
                .addFirstNameElement("user")
                .addSurnameElement("userson")
                .setDateOfBirth(LocalDate.of(1970, 1, 1))
                .build();
    }
}
