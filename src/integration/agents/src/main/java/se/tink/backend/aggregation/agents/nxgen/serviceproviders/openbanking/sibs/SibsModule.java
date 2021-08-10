package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.module.QSealSignatureProviderModuleRsaSha256;

public class SibsModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new QSealSignatureProviderModuleRsaSha256());
    }
}
