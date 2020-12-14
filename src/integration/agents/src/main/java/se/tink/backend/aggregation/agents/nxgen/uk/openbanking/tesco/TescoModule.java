package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;

public class TescoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("eidasCertId")).toInstance("DEFAULT");
        bind(UkOpenBankingConfiguration.class)
                .toProvider(TescoConfigurationProvider.class)
                .in(Scopes.SINGLETON);
    }
}
