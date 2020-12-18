package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.yorkshire;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;

// TODO it's gonna be deleted after migration of the bank to the OBSEAL/OBWAC certificates
//  information from 16.11.2020
// (https://mcusercontent.com/af1c391b805be615ed31708eb/files/d5212d4c-5509-4f6f-a1ac-b60021eb6fec/OBIE_eIDAS_OB_Certificate_TPP_Communication_v1.pdf)
public class YorkshireModule extends AbstractModule {

    public static final String OPEN_BANKING_CERT_ID = "DEFAULT";

    @Override
    protected void configure() {
        bind(UkOpenBankingConfiguration.class).toProvider(YorkshireConfigurationProvider.class);
        bind(String.class)
                .annotatedWith(Names.named("eidasCertId"))
                .toInstance(OPEN_BANKING_CERT_ID);
    }
}
